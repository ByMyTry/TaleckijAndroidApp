package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by Lenovo on 06.02.2018.
 */

public interface OnAppsChangeListener {
    void onAppInstalled(Context context, int addedAppUid);
    void onAppRemoved(Context context, int removedAppUid);
    //void onAppUpdatePosFromDesk(String appFullName, @Nullable Integer currentPos);
}
