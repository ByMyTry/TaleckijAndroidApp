package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppsChangeReceiver extends BroadcastReceiver {
    private final OnAppsChangeListener onAppsChangeListener;

    AppsChangeReceiver(OnAppsChangeListener onAppsChangeListener){
        this.onAppsChangeListener = onAppsChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null) return;
        if(intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            if(!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                int addedAppUid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                onAppsChangeListener.onAppInstalled(addedAppUid);
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            if(!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                int removedAppUid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                onAppsChangeListener.onAppRemoved(removedAppUid);
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)){
            int updatedAppUid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            onAppsChangeListener.onAppRemoved(updatedAppUid);
            onAppsChangeListener.onAppInstalled(updatedAppUid);
        }
    }
}
