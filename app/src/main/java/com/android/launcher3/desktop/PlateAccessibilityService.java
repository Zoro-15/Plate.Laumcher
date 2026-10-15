package com.android.launcher3.desktop;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.Utilities;

public class PlateAccessibilityService extends AccessibilityService {

    private static final String TAG = "PlateAccessibility";
    public static final String PREF_OVERLAY_ENABLED = "pref_desktop_overlay_enabled";

    private static volatile PlateAccessibilityService sInstance;
    private static ServiceStateListener sListener;

    public interface ServiceStateListener {
        void onServiceStateChanged(boolean running);
    }

    public static void setServiceListener(ServiceStateListener listener) {
        sListener = listener;
        if (sListener != null) {
            sListener.onServiceStateChanged(sInstance != null);
        }
    }

    private WindowManager mWindowManager;
    private View mStatusPillOverlay;
    private View mNavPillOverlay;
    private View mDrawerBtnOverlay;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sInstance = this;
        Log.d(TAG, "PlateAccessibilityService connected");
        setupOverlays();
        if (sListener != null) {
            sListener.onServiceStateChanged(true);
        }
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Overlays auto-reposition with gravity, but we re-bind layout margins if needed
    }

    @Override
    public boolean onUnbind(Intent intent) {
        removeOverlays();
        sInstance = null;
        if (sListener != null) {
            sListener.onServiceStateChanged(false);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlays();
        sInstance = null;
        if (sListener != null) {
            sListener.onServiceStateChanged(false);
        }
    }

    public static boolean isServiceRunning() {
        return sInstance != null;
    }

    public static PlateAccessibilityService getInstance() {
        return sInstance;
    }

    public void setupOverlays() {
        SharedPreferences prefs = Utilities.getPrefs(this);
        if (!prefs.getBoolean(PREF_OVERLAY_ENABLED, true)) {
            removeOverlays();
            return;
        }

        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        if (mWindowManager == null) return;

        // Wrap service in launcher theme so layout attributes & styles resolve
        Context themedContext = new ContextThemeWrapper(this, R.style.AppTheme);
        LayoutInflater inflater = LayoutInflater.from(themedContext);

        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();

        int systemUiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // 1. Status Pill Overlay (Top Right)
        if (mStatusPillOverlay == null) {
            try {
                mStatusPillOverlay = inflater.inflate(R.layout.desktop_status_pill, null);
                WindowManager.LayoutParams statusLp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        ResourceUtils.pxFromDp(40, dm),
                        overlayType,
                        flags,
                        PixelFormat.TRANSLUCENT
                );
                statusLp.systemUiVisibility = systemUiFlags;
                statusLp.gravity = Gravity.TOP | Gravity.END;
                statusLp.x = ResourceUtils.pxFromDp(10, dm);
                statusLp.y = 0;
                mStatusPillOverlay.setSystemUiVisibility(systemUiFlags);
                mWindowManager.addView(mStatusPillOverlay, statusLp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to attach status pill overlay", e);
            }
        }

        // 2. Nav Pill Overlay (Bottom Right)
        if (mNavPillOverlay == null) {
            try {
                mNavPillOverlay = inflater.inflate(R.layout.desktop_nav_pill, null);
                WindowManager.LayoutParams navLp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        ResourceUtils.pxFromDp(36, dm),
                        overlayType,
                        flags,
                        PixelFormat.TRANSLUCENT
                );
                navLp.systemUiVisibility = systemUiFlags;
                navLp.gravity = Gravity.BOTTOM | Gravity.END;
                navLp.x = ResourceUtils.pxFromDp(10, dm);
                navLp.y = 0;

                View btnMenu = mNavPillOverlay.findViewById(R.id.btn_nav_menu);
                if (btnMenu != null) {
                    btnMenu.setOnClickListener(v -> openRecents(PlateAccessibilityService.this));
                    btnMenu.setOnLongClickListener(v -> {
                        performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                        return true;
                    });
                }

                View btnHome = mNavPillOverlay.findViewById(R.id.btn_nav_home);
                if (btnHome != null) {
                    btnHome.setOnClickListener(v -> performGlobalAction(GLOBAL_ACTION_HOME));
                }

                View btnBack = mNavPillOverlay.findViewById(R.id.btn_nav_back);
                if (btnBack != null) {
                    btnBack.setOnClickListener(v -> performGlobalAction(GLOBAL_ACTION_BACK));
                }

                mNavPillOverlay.setSystemUiVisibility(systemUiFlags);
                mWindowManager.addView(mNavPillOverlay, navLp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to attach nav pill overlay", e);
            }
        }

        // 3. Drawer Button Overlay (Bottom Left)
        if (mDrawerBtnOverlay == null) {
            try {
                mDrawerBtnOverlay = inflater.inflate(R.layout.desktop_drawer_button, null);
                WindowManager.LayoutParams drawerLp = new WindowManager.LayoutParams(
                        ResourceUtils.pxFromDp(42, dm),
                        ResourceUtils.pxFromDp(36, dm),
                        overlayType,
                        flags,
                        PixelFormat.TRANSLUCENT
                );
                drawerLp.systemUiVisibility = systemUiFlags;
                drawerLp.gravity = Gravity.BOTTOM | Gravity.START;
                drawerLp.x = ResourceUtils.pxFromDp(10, dm);
                drawerLp.y = 0;

                mDrawerBtnOverlay.setOnClickListener(v -> {
                    Launcher launcher = Launcher.getActiveLauncher();
                    if (launcher != null && launcher.hasWindowFocus() && launcher.isStarted()) {
                        AppDrawerPopup.toggle(launcher);
                    } else {
                        Intent homeIntent = new Intent(PlateAccessibilityService.this, Launcher.class);
                        homeIntent.setAction(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        homeIntent.putExtra("extra_open_drawer", true);
                        startActivity(homeIntent);
                    }
                });

                mDrawerBtnOverlay.setSystemUiVisibility(systemUiFlags);
                mWindowManager.addView(mDrawerBtnOverlay, drawerLp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to attach drawer button overlay", e);
            }
        }
        suppressSystemNavigationBar();
    }

    public void removeOverlays() {
        if (mWindowManager != null) {
            if (mStatusPillOverlay != null) {
                try {
                    mWindowManager.removeView(mStatusPillOverlay);
                } catch (Exception ignored) {}
                mStatusPillOverlay = null;
            }
            if (mNavPillOverlay != null) {
                try {
                    mWindowManager.removeView(mNavPillOverlay);
                } catch (Exception ignored) {}
                mNavPillOverlay = null;
            }
            if (mDrawerBtnOverlay != null) {
                try {
                    mWindowManager.removeView(mDrawerBtnOverlay);
                } catch (Exception ignored) {}
                mDrawerBtnOverlay = null;
            }
        }
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

    public void setDrawerButtonVisibility(int visibility) {
        if (mDrawerBtnOverlay != null) {
            mDrawerBtnOverlay.setVisibility(visibility);
        }
    }

    public void suppressSystemNavigationBar() {
        suppressSystemNavigationBar(this);
    }

    public static void suppressSystemNavigationBar(Context context) {
        if (context != null) {
            try {
                Settings.Global.putString(context.getContentResolver(), "policy_control", "immersive.navigation=*");
            } catch (Throwable ignored) {}
        }
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "settings put global policy_control immersive.navigation=*"});
            p.waitFor();
        } catch (Throwable ignored) {}

        try {
            Process p = Runtime.getRuntime().exec("settings put global policy_control immersive.navigation=*");
            p.waitFor();
        } catch (Throwable ignored) {}
    }
}
