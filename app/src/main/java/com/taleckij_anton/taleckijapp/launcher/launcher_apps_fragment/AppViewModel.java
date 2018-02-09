package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * Created by Lenovo on 09.02.2018.
 */

public class AppViewModel {
    private @Nullable
    Drawable mIcon;
    private @Nullable String mLabel;

    public @Nullable String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public @Nullable Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public AppViewModel(@Nullable Drawable icon, @Nullable String label){
        mIcon = icon;
        mLabel = label;
    }
}
