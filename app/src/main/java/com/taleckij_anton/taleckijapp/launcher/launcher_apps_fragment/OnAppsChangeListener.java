package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

/**
 * Created by Lenovo on 06.02.2018.
 */

public interface OnAppsChangeListener {
    void onAppInstalled(int addedAppUid);
    void onAppRemoved(int removedAppUid);
}
