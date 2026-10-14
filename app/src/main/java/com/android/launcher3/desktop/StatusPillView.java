package com.android.launcher3.desktop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.R;

public class StatusPillView extends LinearLayout {

    private TextView mBatteryText;

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                int percent = (level * 100) / scale;
                if (mBatteryText != null) {
                    mBatteryText.setText(percent + " %");
                }
            }
        }
    };

    public StatusPillView(Context context) {
        this(context, null);
    }

    public StatusPillView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusPillView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBatteryText = findViewById(R.id.battery_text);
        setOnClickListener(v -> DesktopHelper.expandQuickSettings(getContext()));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(mBatteryReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getContext().unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) {}
    }
}
