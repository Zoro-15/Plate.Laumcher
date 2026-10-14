package com.android.launcher3.desktop;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.AppInfo;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.DragSource;
import com.android.launcher3.DropTarget;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ResourceUtils;
import com.android.launcher3.dragndrop.DragOptions;
import com.android.launcher3.userevent.nano.LauncherLogProto.Target;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.views.BaseDragLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Desktop start-menu style floating popup card displaying all apps in a 5-column grid.
 */
public class AppDrawerPopup extends AbstractFloatingView implements DragSource {

    private Launcher mLauncher;
    private EditText mSearchInput;
    private RecyclerView mRecyclerView;
    private AppAdapter mAdapter;
    private final List<AppInfo> mFullAppsList = new ArrayList<>();

    public AppDrawerPopup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppDrawerPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSearchInput = findViewById(R.id.drawer_search_input);
        mRecyclerView = findViewById(R.id.drawer_apps_grid);
    }

    public void populateApps(Launcher launcher) {
        mLauncher = launcher;
        mFullAppsList.clear();

        if (launcher.getAppsView() != null && launcher.getAppsView().getAppsStore() != null) {
            mFullAppsList.addAll(launcher.getAppsView().getAppsStore().getApps());
        }

        Collections.sort(mFullAppsList, (a, b) -> {
            String titleA = a.title != null ? a.title.toString() : "";
            String titleB = b.title != null ? b.title.toString() : "";
            return titleA.compareToIgnoreCase(titleB);
        });

        int spanCount = 5;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        mAdapter = new AppAdapter(launcher, mFullAppsList);
        mRecyclerView.setAdapter(mAdapter);

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null) {
                    mAdapter.filter(s != null ? s.toString() : "", mFullAppsList);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static AppDrawerPopup show(Launcher launcher) {
        AppDrawerPopup existing = getOpenView(launcher, TYPE_ACTION_POPUP);
        if (existing != null) {
            return existing;
        }

        View drawerBtn = launcher.findViewById(R.id.btn_desktop_app_drawer);
        if (drawerBtn != null) {
            drawerBtn.setVisibility(View.INVISIBLE);
        }

        BaseDragLayer dragLayer = launcher.getDragLayer();
        AppDrawerPopup popup = (AppDrawerPopup) launcher.getLayoutInflater()
                .inflate(R.layout.desktop_app_drawer_card, dragLayer, false);

        DeviceProfile dp = launcher.getDeviceProfile();
        int width;
        int height;
        android.util.DisplayMetrics dm = launcher.getResources().getDisplayMetrics();
        if (dp.isLandscape) {
            width = Math.min((int) (dp.widthPx * 0.45f), ResourceUtils.pxFromDp(360, dm));
            height = (int) (dp.heightPx * 0.88f);
        } else {
            width = dp.widthPx - ResourceUtils.pxFromDp(16, dm);
            height = (int) (dp.heightPx * 0.65f);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        lp.gravity = Gravity.BOTTOM | Gravity.START;
        lp.leftMargin = ResourceUtils.pxFromDp(2, dm);
        lp.bottomMargin = ResourceUtils.pxFromDp(1, dm);
        popup.setLayoutParams(lp);

        popup.populateApps(launcher);
        dragLayer.addView(popup);
        popup.mIsOpen = true;

        popup.setPivotX(0);
        popup.setPivotY(height);
        popup.setAlpha(0f);
        popup.setTranslationY(ResourceUtils.pxFromDp(25, dm));
        popup.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        return popup;
    }

    public static void toggle(Launcher launcher) {
        AppDrawerPopup existing = getOpenView(launcher, TYPE_ACTION_POPUP);
        if (existing != null) {
            existing.close(true);
        } else {
            show(launcher);
        }
    }

    @Override
    protected void handleClose(boolean animate) {
        if (mIsOpen) {
            mIsOpen = false;
            View drawerBtn = mLauncher != null ? mLauncher.findViewById(R.id.btn_desktop_app_drawer) : null;
            if (drawerBtn != null) {
                drawerBtn.setVisibility(View.VISIBLE);
            }
            if (animate) {
                animate()
                        .alpha(0f)
                        .translationY(ResourceUtils.pxFromDp(25, getResources().getDisplayMetrics()))
                        .setDuration(140)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> {
                            ViewGroup parent = (ViewGroup) getParent();
                            if (parent != null) {
                                parent.removeView(this);
                            }
                        })
                        .start();
            } else {
                ViewGroup parent = (ViewGroup) getParent();
                if (parent != null) {
                    parent.removeView(this);
                }
            }
        }
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            BaseDragLayer dl = ActivityContext.lookupContext(getContext()).getDragLayer();
            if (dl != null && !dl.isEventOverView(this, ev)) {
                close(true);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isOfType(int type) {
        return (type & TYPE_ACTION_POPUP) != 0;
    }

    @Override
    public void logActionCommand(int command) {}

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean success) {}

    @Override
    public void fillInLogContainerData(View v, ItemInfo info, Target target, Target targetParent) {}

    private class AppAdapter extends RecyclerView.Adapter<AppViewHolder> {
        private final List<AppInfo> mItems = new ArrayList<>();
        private final Launcher mLauncherInstance;

        AppAdapter(Launcher launcher, List<AppInfo> items) {
            mLauncherInstance = launcher;
            mItems.addAll(items);
        }

        void filter(String query, List<AppInfo> fullList) {
            mItems.clear();
            if (TextUtils.isEmpty(query)) {
                mItems.addAll(fullList);
            } else {
                String lower = query.toLowerCase().trim();
                for (AppInfo info : fullList) {
                    if (info.title != null && info.title.toString().toLowerCase().contains(lower)) {
                        mItems.add(info);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_desktop_app, parent, false);
            return new AppViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            AppInfo info = mItems.get(position);
            holder.title.setText(info.title);
            if (info.iconBitmap != null) {
                holder.icon.setImageBitmap(info.iconBitmap);
            }
            holder.itemView.setTag(info);
            holder.itemView.setOnClickListener(v -> {
                close(true);
                mLauncherInstance.startActivitySafely(v, info.getIntent(), info, null);
            });
            holder.itemView.setOnLongClickListener(v -> {
                DragOptions options = new DragOptions();
                DeviceProfile grid = mLauncherInstance.getDeviceProfile();
                options.intrinsicIconScaleFactor = (float) grid.allAppsIconSizePx / grid.iconSizePx;
                mLauncherInstance.getWorkspace().beginDragShared(v, AppDrawerPopup.this, options);
                close(false);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;

        AppViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            title = itemView.findViewById(R.id.app_title);
        }
    }
}
