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
        } catch (Exception e) {
            Log.e(TAG, "Failed to expand status bar / quick settings", e);
        }
    }
}
