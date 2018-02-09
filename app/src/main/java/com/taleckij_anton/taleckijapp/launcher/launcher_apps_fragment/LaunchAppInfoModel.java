package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.content.ComponentName;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class LaunchAppInfoModel {
    private final LauncherActivityInfo mLauncherActivityInfo;
    private final AppViewModel mAppViewModel;
    private int mLaunchCount;

    public LaunchAppInfoModel(LauncherActivityInfo launcherActivityInfo, int launchCount,
                                AppViewModel appViewModel){
        mLauncherActivityInfo = launcherActivityInfo;
        mLaunchCount = launchCount;
        mAppViewModel = appViewModel;
    }

    public int getLaunchCount(){
        return mLaunchCount;
    }

    public void incrementLaunchCount(){ mLaunchCount++;
    }

    public String getPackageName(){
        return mLauncherActivityInfo.getComponentName().getPackageName();
    }

    public String getFullName(){
        return mLauncherActivityInfo.getName();
    }

    public long getFirstInstallTime(){
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    public ComponentName getComponentName(){
        return mLauncherActivityInfo.getComponentName();
    }

    public int getUid(){
        return mLauncherActivityInfo.getApplicationInfo().uid;
    }

    public String getLabel(){
        return (String)mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density){
        return mLauncherActivityInfo.getIcon(density);
    }

    public AppViewModel getAppViewModel(){
        return mAppViewModel;
    }
}
