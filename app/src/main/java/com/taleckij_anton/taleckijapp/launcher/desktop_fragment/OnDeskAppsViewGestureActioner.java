package com.taleckij_anton.taleckijapp.launcher.desktop_fragment;

import android.content.Context;
import android.view.View;

import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;

/**
 * Created by Lenovo on 20.02.2018.
 */

public interface OnDeskAppsViewGestureActioner {
    void launchApp(Context context, AppInfoModel appModel);
    void startDrag(View v, AppInfoModel appModel);
    void stopDrag(View v, AppInfoModel appModelWithNewDescPos, boolean changeDeskPos);
    //void showPopup(View v, AppInfoModel appModel);
}
