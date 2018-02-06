package com.taleckij_anton.taleckijapp.launcher.applications;

import android.content.Context;
import android.view.View;

/**
 * Created by Lenovo on 06.02.2018.
 */

public interface OnRecyclerViewGestureActioner {
    void launchApp(Context context, LaunchAppInfoModel appModel);
    void showPopup(View v, LaunchAppInfoModel appModel);
}
