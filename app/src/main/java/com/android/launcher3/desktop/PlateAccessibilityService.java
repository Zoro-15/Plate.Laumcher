package com.android.launcher3.desktop;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.android.launcher3.R;

public class PlateAccessibilityService extends AccessibilityService {

    private static final String TAG = "PlateAccessibility";
    private static volatile PlateAccessibilityService sInstance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        Log.d(TAG, "PlateAccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Passive service, no event consumption needed
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "PlateAccessibilityService interrupted");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    public static boolean isServiceRunning() {
        return sInstance != null;
    }

    public static boolean openRecents(Context context) {
        // 1. If AccessibilityService is enabled, performGlobalAction(GLOBAL_ACTION_RECENTS)
        if (sInstance != null) {
            try {
                boolean success = sInstance.performGlobalAction(GLOBAL_ACTION_RECENTS);
                if (success) {
                    return true;
                }
            } catch (Throwable t) {
                Log.e(TAG, "performGlobalAction failed", t);
            }
        }

        // 2. Try root shell keyevent 187 (KEYCODE_APP_SWITCH)
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "input keyevent 187"});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        // 3. Fallback: prompt user to enable accessibility and open Accessibility Settings
        if (context != null) {
            try {
                Toast.makeText(context, R.string.enable_accessibility_for_recents, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch accessibility settings", e);
            }
        }
        return false;
    }
}
