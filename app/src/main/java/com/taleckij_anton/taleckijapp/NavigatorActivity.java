package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Lenovo on 03.03.2018.
 */

public class NavigatorActivity extends AppCompatActivity {
    public static final String LaunchWpIntentKey = "LaunchWpIntentKey";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = new Intent();
        final Class activityClass;
        if(needLaunchWelcomePage()){
            activityClass = WelcomePageActivity.class;
        } else {
            activityClass = LauncherActivity.class;
        }
        intent.setClass(this, activityClass);
        startActivity(intent);
    }

    private boolean needLaunchWelcomePage(){
        if(getIntent().hasExtra(LaunchWpIntentKey)) {
            return getIntent().getBooleanExtra(LaunchWpIntentKey, false);
        } else {
            final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
//        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
//        final String layoutPrefKey = getResources().getString(R.string.compact_layout_preference_key);
//        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);

//        if(isFirstRunning(themePrefKey, layoutPrefKey)){
//            return true;
//        }
//
//        if(getLaunchWpOnceValue(launchWpPrefKey)){
//            setLaunchWpOnceFalse(launchWpPrefKey);
//            return true;
//        }
            final SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);

            return sharedPreferences.getBoolean(launchWpPrefKey, true);
        }
    }

//    private void launchWelcomePageIfNecessary(){
//        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
//        final String layoutPrefKey = getResources().getString(R.string.compact_layout_preference_key);
//        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
//        if(!thisIsNotFirstRunning(themePrefKey, layoutPrefKey)
//                || (launchWpOncePrefIsTrue(launchWpPrefKey)
//                && !getIntent().getBooleanExtra(CHANGE_THEME_FROM_SETTINGS,false)
//                && !getIntent().getBooleanExtra(LAUNCH_FROM_WELCOME_PAGE,false)
//                && !getIntent().getBooleanExtra(LAUNCH_FROM_PROFILE, false))
//                ){
//            Intent intent = new Intent();
//            intent.putExtra(WelcomePageActivity.LAUNCH_FROM_LAUNCHER, true);
//            intent.setClass(this, WelcomePageActivity.class);
//            startActivity(intent);
//        }
//    }

    private boolean isFirstRunning(String themePrefKey, String layoutPrefKey){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return !(sharedPreferences.contains(themePrefKey) && sharedPreferences.contains(layoutPrefKey));
    }

    private boolean getLaunchWpOnceValue(String launchWpPrefKey){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final boolean launchWpOnce = sharedPreferences.getBoolean(launchWpPrefKey, false);
        return launchWpOnce;
    }

    private void setLaunchWpOnceFalse(String launchWpPrefKey){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(launchWpPrefKey, false)
                .apply();
    }
}
