# Plate Launcher

A lightweight, DeX-inspired desktop launcher for **Nokia 6.1** running **Android 10**, tailored for productivity over **scrcpy**.

---

## Key Highlights

- **Desktop First:** Designed for landscape mode with a persistent taskbar, pinned applications, and quick app switching.
- **Ultra Lightweight:** Forked from pure AOSP Launcher3 Android 10 without heavy background services, telemetry, or complex animations.
- **Untouched ROM Safe:** Operates as a standard user-level `CATEGORY_HOME` launcher without requiring root, system-privilege modification, or replacing the system recents provider.
- **Zero Local Build Overhead:** 100% offloaded to **GitHub Actions CI**. No heavy compilation on low-end host hardware.

---

## Project Documentation

- [Project Context & Principles](projectcontext.md)
- [Phase 1 Roadmap & Architecture](roadmap%20phase%201.md)

---

## Building

This project is automatically built via GitHub Actions on every push to `main`.
Pre-built debug APKs can be downloaded directly from the **Actions** tab of this repository.

To build manually in an Android SDK environment:

```bash
./gradlew assembleDebug
```
