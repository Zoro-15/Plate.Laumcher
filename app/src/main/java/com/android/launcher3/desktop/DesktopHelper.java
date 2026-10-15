package com.android.launcher3.desktop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public final class DesktopHelper {

    private static final String TAG = "DesktopHelper";

    private DesktopHelper() {}

    /**
     * Expands the Quick Settings panel, falling back to notifications panel.
     */
    public static void expandQuickSettings(Context context) {
        if (context == null) return;
        try {
            @SuppressLint("WrongConstant")
            Object service = context.getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            try {
                Method expand = statusbarManager.getMethod("expandSettingsPanel");
                expand.invoke(service);
            } catch (NoSuchMethodException e) {
                Method expand = statusbarManager.getMethod("expandNotificationsPanel");
                expand.invoke(service);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to expand status bar / quick settings", t);
        }
    }

    /**
     * Toggles Android's Recent Apps / Overview menu.
     */
    public static void toggleRecentApps(com.android.launcher3.Launcher launcher) {
        if (launcher == null) return;

        // 1. Try AccessibilityService (standard & reliable for Android 7-15)
        if (PlateAccessibilityService.openRecents(launcher)) {
            return;
        }

        // 2. Try IStatusBarService via ServiceManager (if running as system/priv-app)
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getMethod("getService", String.class);
            android.os.IBinder binder = (android.os.IBinder) getService.invoke(null, "statusbar");
            if (binder != null) {
                Class<?> stubClass = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub");
                Method asInterface = stubClass.getMethod("asInterface", android.os.IBinder.class);
                Object statusBarService = asInterface.invoke(null, binder);
                Method toggle = statusBarService.getClass().getMethod("toggleRecentApps");
                toggle.invoke(statusBarService);
                return;
            }
        } catch (Throwable t) {
            Log.d(TAG, "IStatusBarService.toggleRecentApps failed", t);
        }

        // 3. Try StatusBarManager.toggleRecentApps
        try {
            @SuppressLint("WrongConstant")
            Object service = launcher.getSystemService("statusbar");
            Class<?> smClass = Class.forName("android.app.StatusBarManager");
            Method toggle = smClass.getMethod("toggleRecentApps");
            toggle.invoke(service);
            return;
        } catch (Throwable t) {
            Log.d(TAG, "StatusBarManager.toggleRecentApps failed", t);
        }

        // 4. Fallback to Launcher Overview state
        try {
            launcher.getStateManager().goToState(com.android.launcher3.LauncherState.OVERVIEW);
        } catch (Throwable t) {
            Log.e(TAG, "LauncherState.OVERVIEW failed", t);
        }
    }
}
