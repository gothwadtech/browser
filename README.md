<div align="center">

# 🌐 Gothwad Browser
### *The Fastest, Securest & Ultra-Lightweight Web Browser for Android TVs, Tablets & Phones*

[![Platform](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Tablets%20%7C%20Phones-4CAF50?style=for-the-badge&logo=android)](https://developer.android.com/tv)
[![Engine](https://img.shields.io/badge/Engine-Native%20Chromium%20WebView-4285F4?style=for-the-badge&logo=google-chrome)](https://developer.android.com/develop/ui/views/layout/webapps/webview)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Target Size](https://img.shields.io/badge/APK%20Footprint-%3C%2010%20MB-FF5722?style=for-the-badge)](#-engineering-journey--vision)
[![Made in India](https://img.shields.io/badge/Made%20in%20India-🇮🇳%20With%20Love-FF9933?style=for-the-badge)](#-author--credits)
[![License: Proprietary](https://img.shields.io/badge/License-Source--Available%20%2F%20All%20Rights%20Reserved-red.svg?style=for-the-badge)](#-proprietary-notice--usage-terms)

<br/>

> **"Gothwad Browser is the fastest, securest, and most lightweight browser for Android Phones & TVs you have ever seen. Advanced features engineered specifically for physical keyboard and mouse on Android TVs, with industry-leading support for TV remote D-Pad navigation. Built for genuine multitasking with an inbuilt File Manager, TV Notepad, and Clipboard Manager. Try first, then believe."**  
> — *Made in India with ❤️ by Gothwad Tech*

</div>

---

> [!CAUTION]
> ### ⚖️ PROPRIETARY NOTICE & USAGE RESTRICTIONS
> **Copyright © Gothwad Tech. All Rights Reserved.**  
> The source code in this repository is published strictly as **Source-Available for inspection, security auditing, and viewing purposes only**.  
> **NO OPEN SOURCE LICENSE IS GRANTED.**  
> - You **MAY NOT** copy, clone, distribute, redistribute, modify, sub-license, reverse-engineer, publish, or monetize this code or any part of it.
> - You **MAY NOT** use this software or its components in any commercial, open-source, or non-commercial product or project.
> - Any unauthorized use, extraction, reproduction, or redistribution of this codebase is strictly prohibited and constitutes an infringement of copyright laws.

---

## 📑 Table of Contents
- [✨ Core Highlights](#-core-highlights)
- [🎮 Complete Input & Hardware Parity](#-complete-input--hardware-parity)
- [🧰 Integrated Productivity Suite](#-integrated-productivity-suite)
- [🚀 Genesis & Engineering Journey](#-genesis--engineering-journey)
- [🏗️ Architecture & Philosophy](#️-architecture--philosophy)
- [🗺️ Development Roadmap](#️-development-roadmap)
- [📱 Supported Platforms & Form Factors](#-supported-platforms--form-factors)
- [⚖️ Proprietary Notice & Usage Terms](#-proprietary-notice--usage-terms)
- [❤️ Author & Credits](#️-author--credits)

---

## ✨ Core Highlights

Gothwad Browser is built with a singular mission: **bring desktop PC Chrome-grade browsing to Android TV and mobile screens without the bloat.**

- ⚡ **Ultra-Fast & Featherweight:** Engineered to stay under a **10 MB APK footprint**, booting instantaneously even on budget TV streaming sticks with limited RAM (1GB/2GB).
- 🖥️ **PC-Grade Chromium Engine:** Native Chromium rendering fidelity. No synthetic CSS zooms, no layout reflow compromises, and no artificial color shifts—pages render exactly as intended.
- 🛡️ **Integrated Ad & Tracker Protection:** Fast, real-time filter-list ad blocking (Brave `AdBlockClient` engine) directly intercepting network resources asynchronously without degrading render performance.
- 🗂️ **Advanced Tab Architecture:** Dynamic top-tab bar and row/grid tab switchers with proactive LRU memory trimming so you can keep 10+ tabs without background crashes.
- 🔒 **App Lock & Privacy:** Built-in PIN security lock to safeguard your browsing sessions, private files, notes, and history.
- 🌐 **Global Accessibility:** Native localized interfaces across 10+ languages with responsive 10-foot TV UI ergonomics.

---

## 🎮 Complete Input & Hardware Parity

Browsing on Android TV is often plagued by clunky remote controls or broken mouse input. Gothwad Browser is engineered with a **triple-input pipeline** where every input mechanism functions at full fidelity without interfering with another:

| Input Method | Supported Behaviors & Capabilities |
|---|---|
| **TV Remote / D-Pad** | Smooth on-screen virtual cursor with proportional acceleration, intelligent directional focus navigation across headers, tabs, and web elements. |
| **Physical USB / Bluetooth Mouse** | Native Chromium mouse events (`SOURCE_MOUSE`), pixel-accurate hover states, right-click contextual menus, native click-drag blue text selection, and fluid scroll-wheel physics. |
| **Physical Keyboard** | Full hardware shortcut suite (`Ctrl+T` new tab, `Ctrl+W` close tab, `Ctrl+Tab` switch tabs, `Ctrl+R` refresh, etc.) with pure-modifier release protection. |
| **Touchscreen (Tablets & Phones)** | Multi-touch gestures, fluid pinch-to-zoom (GPU-accelerated), and responsive edge-to-edge touch layouts. |

---

## 🧰 Integrated Productivity Suite

Gothwad Browser eliminates the need to switch between multiple utility apps on your TV:

1. **📁 Inbuilt File Manager:**
   - Explore local TV storage, external USB drives, and download directories directly from the browser sidebar.
   - Quick preview, file deletion, and direct APK/media launching on large screens.
2. **📝 TV Notepad & Notes:**
   - Jot down quick thoughts, recipes, URLs, streaming links, or cheat codes while browsing.
   - Accessible at any time via the quick-action sidebar without losing your current web tab.
3. **📋 Smart Clipboard Manager:**
   - Automatically captures copied text and links from web pages.
   - Quick-access snippets and history with duplicate suppression and incognito-mode safety.
4. **📥 Robust Downloads Manager:**
   - Dedicated download orchestrator with background service persistence (`BrowserKeepAliveService`) preventing the TV OS from killing long downloads.

---

## 🚀 Genesis & Engineering Journey

Gothwad Browser was born out of frustration with existing TV browsers that were either sluggish, filled with intrusive ads, or incapable of handling basic mouse and keyboard inputs.

```
┌──────────────────────────────┐
│  Phase 1: Inception          │  Prototyped in Google AI Studio
│  (First 2 Weeks)             │  Rapid concept validation & architectural foundations
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Phase 2: Deep Hardening     │  Shifted to Antigravity & Native Android Studio
│  (~60% AI + Human Synergy)   │  Native Chromium hooks, zero-jugaad input pipelines, Room DB
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Phase 3: Rigorous Testing   │  1 Month of continuous testing on Emulators
│  (Pre-Launch Validation)     │  100+ Real Devices (Android TV boxes, sticks, tablets) Passed
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│  Target Outcome              │  Blazing-fast, feature-packed browser
│  (Production Standard)       │  Ultra-lightweight under 10MB APK
└──────────────────────────────┘
```

- **The AI Studio Foundation:** The initial architecture was scaffolded in **Google AI Studio** during the first two weeks, quickly validating the tab management, input routing, and core UI abstractions.
- **The Shift to Antigravity & Android Studio:** Development was transitioned to **Antigravity** and local **Android Studio** for deep native optimization. Over 60% of the codebase was developed through precision AI-augmented engineering, coupled with rigorous human systems programming.
- **Battle-Tested Across 100+ Real Devices:** Before its first public release, Gothwad Browser underwent over a month of stress testing on emulators and **100+ physical hardware configurations**—including low-end Amlogic, Allwinner, and Rockchip TV boxes, certified Google TV dongles, Fire OS streaming devices, and Android tablets.
- **The Golden Engineering Rule:** **"Zero Jugaad — Native Platform First."** Every workaround (synthetic touch conversion, CSS zoom hacks, forced algorithmic dark mode) was eradicated in favor of clean, native Android and Chromium APIs.
- **The Grand Vision:** An all-in-one, productive, feature-dense web browser that never compromises on performance and stays strictly **under 10 MB**.

---

## 🏗️ Architecture & Philosophy

```
app/src/main/java/
 ├── webengine/              # Chromium WebView encapsulation & lifecycle
 │    ├── WebViewEx.kt       # Native event dispatch, zoom, desktop mode
 │    └── WebViewClients.kt  # onRenderProcessGone crash resilience & page hooks
 ├── activity/main/          # Core TV & Mobile UI Activities & Sidebars
 │    ├── TabsModel.kt       # LRU memory management & proactive tab trimming
 │    └── MainActivityDpad.kt# TV remote focus-routing & shortcut mapping
 ├── widgets/cursor/         # Precision virtual D-Pad cursor overlay
 ├── notes/                  # Inbuilt TV Notepad & Room database
 ├── clipboard/              # Clipboard Manager with feedback-loop guarding
 └── service/keepalive/      # Foreground keep-alive service for TV multitasking
```

- **Clean MVVM + Clean Architecture:** High cohesion, low coupling, and zero blocking calls on the UI or WebView threads.
- **Crash Immunity:** Full recovery via `onRenderProcessGone`—if a heavy JavaScript/WASM tab crashes the renderer, the foreground tab is safely reloaded while background tabs are silently trimmed.
- **Memory Bounded:** Proactive LRU tab unloading keeps memory usage within safe thresholds, even on 1GB RAM TV boxes.

---

## 🗺️ Development Roadmap

- [x] **Milestone 1: Core Navigation & Big-Screen Foundations** *(Completed)*
  - Native Chromium WebView engine with hardware acceleration.
  - Virtual D-Pad cursor for TV remotes with proportional speed.
  - Full hardware keyboard and mouse event passthrough.
  - Inbuilt TV Notepad, Clipboard History, and File Manager.
- [x] **Milestone 2: Real-Device Hardening & Ad-Blocking** *(Completed)*
  - Filter-list-based AdBlock integration.
  - LRU memory tab trimming & crash recovery.
  - 100+ real Android TV hardware validation.
- [ ] **Milestone 3: Performance Hardening & Sub-10MB Seal** *(Current Focus)*
  - Resource optimization and binary shrinking to guarantee sub-10MB release APK.
  - Enhanced video player controls optimized for TV remotes.
  - Faster cold-boot and warm-tab restoration times.
- [ ] **Milestone 4: Cloudless Local Sharing & Extensions Hub** *(Upcoming)*
  - QR-based instant URL & file transfer between phones and TVs (no cloud required).
  - Custom user-script / lightweight extension ecosystem.
  - Advanced backup & restore for bookmarks, notes, and shortcuts.

---

## 📱 Supported Platforms & Form Factors

- **Android TV / Google TV:** Android 5.0 (API 21) up to Android 14+ (Certified & Non-certified boxes, smart TVs, projector boxes, streaming sticks).
- **Android Tablets:** Large-screen split-views, multi-window mode, keyboard dock & mouse support.
- **Android Phones:** Responsive vertical handheld experience with bottom/top navigation.

---

## ⚖️ Proprietary Notice & Usage Terms

```
PROPRIETARY SOURCE-AVAILABLE NOTICE
==================================

1. OWNERSHIP & COPYRIGHT:
   All source code, designs, assets, and branding contained in this repository
   are the proprietary intellectual property of Gothwad Tech.
   Copyright (c) Gothwad Tech. All rights reserved.

2. ALLOWED USAGE:
   - This repository is made public exclusively for code inspection, educational
     review, and architectural study.

3. STRICT PROHIBITIONS:
   - NO LICENSE is granted to compile, repackage, distribute, or publish APKs or
     derivatives of this application on Google Play, Amazon Appstore, F-Droid,
     or any third-party app store or website.
   - NO commercial or non-commercial reproduction, cloning, white-labeling, or
     forking for public deployment is permitted.
   - Any violation of these terms will be met with immediate legal action and DMCA
     takedowns.

Inquiries and permissions: Contact Gothwad Tech.
```

---

## ❤️ Author & Credits

- **Creator & Lead Developer:** **Gothwad Tech** (Pawan Gothwad)
- **Origin:** Proudly conceived, engineered, and built in **India 🇮🇳**
- **Contact:** [pawangothwad@gmail.com](mailto:pawangothwad@gmail.com)

<div align="center">
  <sub>Built with relentless passion for speed, privacy, and big-screen productivity.</sub>
</div>

