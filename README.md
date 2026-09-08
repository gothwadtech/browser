<div align="center">

# 🌐 Gothwad Browser

**The Fastest, Most Secure, Ultra-Lightweight Web Browser for Android TVs, Tablets & Phones.**

*Engineered for desktop-grade browsing, power multitasking, and flawless input handling on every Android screen.*

[![Platform](https://img.shields.io/badge/Platform-Android%20TV%20%7C%20Tablets%20%7C%20Phones-blue.svg?style=flat-square)](https://github.com/gothwadtech/tvbrowser)
[![APK Size](https://img.shields.io/badge/APK%20Size-%3C%2010%20MB-success.svg?style=flat-square)](#ultra-lightweight-architecture)
[![Made in India](https://img.shields.io/badge/Made%20in-India%20%F0%9F%87%AE%E2%85%B3-orange.svg?style=flat-square)](https://github.com/gothwadtech)
[![Tested Devices](https://img.shields.io/badge/Real%20Devices%20Passed-100%2B-brightgreen.svg?style=flat-square)](#development-journey--roadmap)

---

> **"Try First Then Believe."**  
> Specially crafted with love by **Gothwad Tech** 🇮🇳

</div>

---

## 📌 Important Legal Notice & Code Viewing Terms

> ### ⚠️ PROPRIETARY SOURCE CODE — STRICTLY FOR VIEWING ONLY
> **All Rights Reserved © Gothwad Tech.**
>
> The source code in this repository is made publicly accessible solely for transparency, reference, and educational viewing purposes.
> - **NO LICENSE GRANTED:** This repository does **NOT** convey any license (open-source or proprietary) under copyright, patent, trademark, or any other intellectual property laws.
> - **UNAUTHORIZED USE STRICTLY PROHIBITED:** You may **NOT** copy, modify, distribute, reproduce, sublicense, decompile, repackage, clone, or use this source code (or any portion of it) in any commercial or non-commercial product, app, library, or derivative work.
> - Any unauthorized deployment, resale, replication, or distribution of this code or its binary builds is strictly unlawful and will be subject to legal action.

---

## 🚀 Overview

**Gothwad Browser** is not just a TV app that happens to browse the web — it is an uncompromising, **desktop-grade browser** built to meet and exceed the performance of modern desktop browsers on Android hardware.

Whether navigating with an **Android TV Remote (D-pad)**, typing on a **Physical Bluetooth/USB Keyboard**, clicking and selecting text with a **Mouse**, or tapping on a **Tablet / Phone touchscreen**, Gothwad Browser delivers native 60fps responsiveness without compromise.

---

## ✨ Key Features & Capabilities

### 🎮 Groundbreaking Multi-Input Support
- **Precision Virtual Pointer / Cursor:** Smooth, customizable on-screen cursor engine for fluid TV remote navigation.
- **True Native Mouse Parity:** Real mouse event passthrough supporting native text selection, drag-and-drop, scroll-wheel scrolling, and right-click context menus.
- **Physical Keyboard Power:** Full shortcut engine with customizable keybindings, modifier combinations (Ctrl, Alt, Shift), and rapid address bar focus.
- **Seamless Remote Integration:** Fast D-pad navigation between header toolbar, home shortcuts, and web content.

### ⚡ Built-in Productivity Powerhouse (All-in-One)
- 📁 **In-App File Manager:** Browse local storage, external USB drives, and download folders directly.
- 📝 **TV Notes & Scratchpad:** Keep quick notes, links, and snippets synchronized alongside your browsing tabs.
- 📋 **Integrated Clipboard Manager:** Automatic history capture for copied links and selected text with foreground privacy protections.
- 🗂️ **Advanced Tab Management:** Instant tab switching with top tab bar and visual tab switcher dialog; proactive LRU background tab memory trimming.
- 🛡️ **Native Ad & Tracker Blocking:** High-efficiency engine powered by Brave's filter client for clean, ad-free page loading.
- 🔒 **App Lock & PIN Security:** Protect browsing privacy, downloads, and history with an on-screen TV-friendly PIN lock.
- 🕶️ **Incognito Browsing Mode:** Clean, isolated browsing sessions leaving zero traces in history or cache.

---

## 🏎️ Ultra-Lightweight Architecture (< 10 MB)

Unlike heavy Chromium forks that bloat to 150MB–250MB, Gothwad Browser is engineered with extreme performance hygiene:
- **Direct Native Engine:** Built on Android's native hardware-accelerated WebView engine with zero synthetic translation lag.
- **Resource Efficient:** Proactive LRU memory trimming prevents tab crashes even on low-RAM (1GB/2GB) smart TV boxes and streaming sticks.
- **Sub-10 MB Footprint:** Downloaded in seconds, launches instantly, and consumes minimal system resources.

---

## 🗺️ Development Journey & Roadmap

```
Phase 1: Concept & Rapid Prototyping
├── Initial architecture prototyped on Google AI Studio (~60% AI-assisted code generation)
└── First 2 weeks focused on UI shell, basic navigation, and modular concept verification.

Phase 2: Transition to AntiGravity & Android Studio
├── Migrated to AntiGravity agentic workflow and native Android Studio tooling
├── Architecture overhaul: Room DB, memory lifecycle, keepalive service, and robust input drivers
└── 1 full month of rigorous emulator & performance stress testing.

Phase 3: Real Device Verification & Production Hardening
├── 100+ real Android TV boxes, streaming sticks, tablets, and phones tested
├── Solved native mouse-drag text selection, zero-jank zoom, and onRenderProcessGone crash immunity
└── Reached 100% test pass rate across TV remote, physical keyboard, mouse, and touch inputs.

Phase 4: Target & Future Horizons
├── Ultra-lightweight ceiling maintained strictly under 10 MB APK
├── Enhanced multi-tab audio playback management
└── Continuous performance and ad-blocking filter updates.
```

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% Modern Kotlin
- **Build System:** Gradle (Kotlin DSL `.gradle.kts`) with multi-module architecture (`:app`, `:app:common`, `buildSrc`)
- **Persistence:** Android Jetpack Room Database (Coroutines + Flow)
- **Engine Layer:** Android System WebView (Chromium-based) with custom GPU-scale zoom & native event pipeline
- **Concurrency:** Kotlin Coroutines & Lifecycle-aware components

---

## 📱 Hardware & OS Compatibility

| Device Type | Primary Input | Tested & Certified |
|---|---|---|
| **Android TVs & Set-Top Boxes** | TV Remote (D-pad) / Air Mouse | ✅ Certified |
| **Android TV with Peripherals** | USB / Bluetooth Keyboard & Mouse | ✅ Certified |
| **Android Tablets & Foldables** | Multi-touch / Stylus / Keyboard Folio | ✅ Certified |
| **Android Smartphones** | One-handed touch / Fullscreen | ✅ Certified |

---

## 📬 Contact & Feedback

Developed and maintained with pride by **Gothwad Tech**.  
For inquiries, bug reports, and feedback:
- **Website / Repo:** [github.com/gothwadtech/tvbrowser](https://github.com/gothwadtech/tvbrowser)
- **Contact:** [pawangothwad@gmail.com](mailto:pawangothwad@gmail.com)
