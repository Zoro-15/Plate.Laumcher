# Plate Launcher: Developer & Agent Navigation Guide (`guide.md`)

This guide serves as a fast index and architectural cheat-sheet for developers and AI agents working on **Plate Launcher** (a lightweight, DeX-inspired launcher for Android 10, running on Nokia 6.1 via `scrcpy`).

---

## 1. Golden Rules & Environment Constraints

> [!IMPORTANT]
> 1. **Do NOT run local Gradle builds on the host machine:** The host laptop has an ultra-low-end Pentium processor. All compilations run via **GitHub Actions CI** (`.github/workflows/android.yml`).
> 2. **Target Platform:** Android 10 (API 29, `minSdkVersion 26`). No root or system-priv app privileges required.
> 3. **Minimal LOC Principle:** Reuse existing AOSP Launcher3 mechanisms (package discovery, icon caching, workspace DB) rather than creating new frameworks or dependencies.
> 4. **Method Signatures:** When launching activities from custom UI, always call `Launcher.startActivitySafely(View v, Intent intent, ItemInfo item, @Nullable String sourceContainer)`.

---

## 2. Fast File Locator

### A. Custom Desktop UI Components (`com.android.launcher3.desktop.*`)

| File Path | Description / Key Contents |
| :--- | :--- |
| [`app/src/main/java/com/android/launcher3/desktop/DesktopHelper.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/desktop/DesktopHelper.java) | Reflection helper for expanding Android Quick Settings (`expandSettingsPanel`) and notification panel. |
| [`app/src/main/java/com/android/launcher3/desktop/StatusPillView.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/desktop/StatusPillView.java) | Upper-right floating status pill widget. Handles live `TextClock` and `ACTION_BATTERY_CHANGED` broadcast receiver. |
| [`app/src/main/java/com/android/launcher3/desktop/AppDrawerPopup.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/desktop/AppDrawerPopup.java) | Floating Start Menu / App Drawer popup card extending `AbstractFloatingView`. Contains instant search filtering and responsive apps grid (8 columns in landscape, 4 in portrait). |

---

### B. Core Launcher Activity & Workspace (`com.android.launcher3.*`)

| File Path | Description / Key Contents |
| :--- | :--- |
| [`app/src/main/java/com/android/launcher3/Launcher.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/Launcher.java) | **Central Activity.** `setupViews()` configures custom desktop buttons (App Drawer toggle, Menu, Home, Back) and hides unused legacy views (`mHotseat`, `page_indicator`). Implements `startActivitySafely`. |
| [`app/src/main/java/com/android/launcher3/Workspace.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/Workspace.java) | Multi-page home desktop canvas. Handles icon placement, folder creation, and dragging. |
| [`app/src/main/java/com/android/launcher3/CellLayout.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/CellLayout.java) | Coordinate grid system representing cells on each workspace page. |
| [`app/src/main/java/com/android/launcher3/DeviceProfile.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/DeviceProfile.java) | Grid metric calculations, icon sizes, and landscape vs portrait dimensions. `isVerticalBarLayout()` determines side dock behavior. |
| [`app/src/main/java/com/android/launcher3/InvariantDeviceProfile.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/InvariantDeviceProfile.java) | Grid definitions (number of rows/columns on desktop and hotseat). |
| [`app/src/main/java/com/android/launcher3/AbstractFloatingView.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/AbstractFloatingView.java) | Base class for floating cards/popups. Handles outside-touch dismissals and back key integration. |
| [`app/src/main/java/com/android/launcher3/AppInfo.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/AppInfo.java) | Data model for installed applications (`title`, `iconBitmap`, `intent`, `componentName`). |
| [`app/src/main/java/com/android/launcher3/ItemInfo.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/ItemInfo.java) | Base item model containing cell coordinates (`cellX`, `cellY`, `screenId`, `container`). |

---

### C. Touch Gestures & Orientation

| File Path | Description / Key Contents |
| :--- | :--- |
| [`app/src/main/java/com/android/launcher3/touch/WorkspaceTouchListener.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/touch/WorkspaceTouchListener.java) | Handles empty desktop touches. `onFling()` detects **swipe down** (expands Quick Settings) and **swipe up** (opens App Drawer popup card). |
| [`app/src/main/java/com/android/launcher3/touch/ItemClickHandler.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/touch/ItemClickHandler.java) | Universal click handler for workspace icons, folders, and shortcuts. |
| [`app/src/main/java/com/android/launcher3/states/RotationHelper.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/states/RotationHelper.java) | Screen rotation controller. `getAllowRotationDefaultValue()` returns `true` for auto-rotation on phones. |
| [`app/src/main/java/com/android/launcher3/uioverrides/AllAppsSwipeController.java`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/uioverrides/AllAppsSwipeController.java) | Legacy full-screen all apps swipe listener. `canInterceptTouch` returns `false` to avoid interfering with desktop gestures. |

---

### D. UI Layouts (`app/src/main/res/layout/`)

| Layout File | Description / Key Elements |
| :--- | :--- |
| [`launcher.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/layout/launcher.xml) | Root layout (`DragLayer`). Includes `workspace`, `desktop_status_pill`, `btn_desktop_app_drawer`, and `desktop_nav_pill`. |
| [`desktop_status_pill.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/layout/desktop_status_pill.xml) | Upper-right pill container: `TextClock` + separator `\|` + `ic_battery` + percentage `TextView`. |
| [`desktop_nav_pill.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/layout/desktop_nav_pill.xml) | Bottom-right pill container: Menu button (`=`), Home button (house icon), Back button (`<`). |
| [`desktop_app_drawer_card.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/layout/desktop_app_drawer_card.xml) | Bottom-left floating card: Search input box (`drawer_search_input`) + apps grid (`drawer_apps_grid`). |
| [`item_desktop_app.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/layout/item_desktop_app.xml) | Grid item layout for each application: `app_icon` (`ImageView`) + `app_title` (`TextView`). |

---

### E. Drawables & Shapes (`app/src/main/res/drawable/`)

| Drawable File | Description |
| :--- | :--- |
| [`bg_desktop_pill.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/bg_desktop_pill.xml) | Dark rounded pill background (`#CC18181B`, corner radius 18dp, 1dp subtle border). |
| [`bg_desktop_circle.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/bg_desktop_circle.xml) | Dark circular shape for bottom-left App Drawer button. |
| [`bg_app_drawer_card.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/bg_app_drawer_card.xml) | Dark floating card background (`#E618181C`, corner radius 18dp, 1dp subtle border). |
| [`bg_search_input.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/bg_search_input.xml) | Semi-translucent background for search box inside App Drawer card. |
| [`ic_battery.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/ic_battery.xml) | Vector battery icon (replaces prototype triangle $\Delta$). |
| [`ic_nav_home.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/ic_nav_home.xml) | Vector home icon (replaces prototype $\pi$). |
| [`ic_nav_menu.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/ic_nav_menu.xml) | Vector menu (`=`) icon for navigation pill. |
| [`ic_nav_back.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/ic_nav_back.xml) | Vector back (`<`) icon for navigation pill. |
| [`ic_apps_grid.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/drawable/ic_apps_grid.xml) | Vector 3×3 grid dots icon for bottom-left App Drawer button. |

---

### F. Configuration & Manifest

| File Path | Description / Key Settings |
| :--- | :--- |
| [`app/src/main/AndroidManifest.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/AndroidManifest.xml) | Defines `CATEGORY_HOME`, permissions (including `EXPAND_STATUS_BAR`), and `screenOrientation="unspecified"`. |
| [`app/src/main/res/values/config.xml`](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/res/values/config.xml) | `allow_rotation = true` (enables rotation), `hotseat_transpose_layout_with_orientation = false` (prevents side vertical bar in landscape). |
| [`.github/workflows/android.yml`](file:///c:/Users/ok/Documents/Plate.Launcher/.github/workflows/android.yml) | Cloud CI build compiling `PlateLauncher-debug.apk` with JDK 17 on Ubuntu runner and uploading the APK artifact. |
| [`roadmap phase 1.md`](file:///c:/Users/ok/Documents/Plate.Launcher/roadmap%20phase%201.md) | High-level roadmap, phase checklist, and architectural entry points. |

---

## 3. Common Development Tasks & How-To

### How to adjust desktop button click behaviors
Open [Launcher.java](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/Launcher.java#L1145-L1185) inside `setupViews()`. The listeners for `btn_desktop_app_drawer`, `btn_nav_menu`, `btn_nav_home`, and `btn_nav_back` are all wired there.

### How to adjust the App Drawer grid layout
Open [AppDrawerPopup.java](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/desktop/AppDrawerPopup.java). 
* To change column count: modify `spanCount` in `populateApps()`.
* To change popup size/position: modify `lp.width`, `lp.height`, `lp.leftMargin`, and `lp.bottomMargin` in `show()`.

### How to adjust swipe gestures
Open [WorkspaceTouchListener.java](file:///c:/Users/ok/Documents/Plate.Launcher/app/src/main/java/com/android/launcher3/touch/WorkspaceTouchListener.java#L165-L185). The velocity threshold and actions for vertical fling (down = Quick Settings, up = App Drawer) are defined in `onFling()`.
