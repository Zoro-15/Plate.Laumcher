# Project Context: Lightweight DeX-Inspired Launcher for Nokia 6.1

## 1. Goal

I want to build a lightweight, DeX-inspired desktop launcher for my Nokia 6.1, primarily used through scrcpy on my laptop. I want desktop-style navigation without the resource overhead of third-party desktop-mirroring applications.

## 2. Hardware and Environment

- Phone: Nokia 6.1 (PL2, Snapdragon 630, 3 GB RAM).
- Operating system: Android 10 custom ROM.
- Host: Old laptop with a 2011-era Intel Pentium processor.
- Phone-to-PC interaction: scrcpy,  over USB.
- Development environment: Antigravity IDE with AI-assisted coding.
- Performance is important.

## 3. Desired Experience

Create a desktop-like Android home screen inspired by Samsung DeX and AOSP desktop mode.

- Landscape-oriented desktop layout.
- Persistent bottom taskbar.
- Pinned application icons.
- Searchable, grid-based app drawer.
- Mouse-friendly navigation and keyboard shortcuts.
- Lightweight appearance without unnecessary visual effects.
- Simple desktop-mode/normal-home switching.
- Potential running-app indicators and convenient app switching, subject to Android API limitations.

## 4. Explicit Non-Goals

- Do NOT implement resizable or floating application windows.
- Do NOT recreate Samsung DeX completely.
- Do NOT build another desktop-mirroring application.
- Do NOT add Electron, browser-based UI frameworks, background services or unnecessary dependencies.
- Do NOT prioritize animations, blur, complex widgets or extensive customization features.
- Do NOT sacrifice stability merely to make the interface look more sophisticated.

## 5. Repository Strategy

My preferred approach is to fork and simplify an existing launcher rather than recreate all launcher functionality from scratch.

Primary candidate:

- AOSP Launcher3, Android 10 release branch:
  [https://android.googlesource.com/platform/packages/apps/Launcher3/+/refs/heads/android10-release/](https://android.googlesource.com/platform/packages/apps/Launcher3/+/refs/heads/android10-release/)

Alternative:

- Lawnchair Launcher, historical `10-dev` branch:
  [https://github.com/LawnchairLauncher/lawnchair](https://github.com/LawnchairLauncher/lawnchair)

Evaluate the actual source, branch history, dependencies, build requirements and licensing before choosing. Do not assume Lawnchair `10-dev` is automatically better or faster because it is older, or that a newer branch is more efficient.

Prefer Android 10 AOSP Launcher3 if it can be built and adapted without disproportionate effort. Consider Lawnchair `10-dev` if its standalone build and existing launcher infrastructure make development substantially easier.

## 6. Technical Direction

- Prefer Kotlin and/or Java with native Android Views.
- Preserve existing, proven launcher functionality wherever practical.
- Reuse Android's package discovery, activity launching and workspace mechanisms.
- Keep the architecture modular and the dependency footprint small.
- Avoid unnecessary rendering, animations and continuous background work , dont run backgroud test or work in in user pc as it is very low end.
- Maintain compatibility with Android 10 .
- Determine whether the source builds as a standalone APK or requires integration into the Android source tree.
- Preserve the upstream baseline and keep custom modifications in clearly separated commits or branches.


Do not assume an ordinary third-party app can silently change Android's default HOME application. Prefer supported APIs and explicit user consent. Avoid framework or system-server modifications unless an essential requirement proves impossible otherwise.

## 7. Development Workflow

Proceed incrementally:

1. Inspect candidate repositories and determine the most suitable baseline.
2. Establish the original build environment and compile an unmodified baseline.
3. Install the baseline on the Nokia 6.1 and verify normal launcher functionality.
4. Identify feature dependencies before removing code.
5. Remove unwanted features incrementally, building and testing after each meaningful change.
6. Implement the desktop layout and taskbar.
7. Add app drawer search, pinned apps and keyboard/mouse interactions.
8. Add desktop-mode switching and optional integrations.
9. Measure resource use and responsiveness instead of assuming that fewer lines of code guarantee better performance.

## 9. AI Coding Requirements - ask user if situation occurs to break this restrictions

- Inspect existing source before proposing modifications.
- Do not invent classes, APIs, files or build commands.
- Explain important dependencies and compatibility risks.
- Make small, reviewable changes instead of large blind rewrites.
- After each change, compile and report actual errors.
- Do not suppress build errors, bypass checks or remove dependencies merely to force a successful build.
- Distinguish confirmed facts from assumptions.
- Avoid rewriting working launcher components unnecessarily.

