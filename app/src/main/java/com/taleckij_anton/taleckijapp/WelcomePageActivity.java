package com.taleckij_anton.taleckijapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.crashlytics.android.Crashlytics;
import com.taleckij_anton.taleckijapp.welcope_page.SettingsWpFragment;
import com.taleckij_anton.taleckijapp.welcope_page.SimpleWpFragment;
import com.taleckij_anton.taleckijapp.welcope_page.WpFragment;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class WelcomePageActivity extends AppCompatActivity {

    private final static String SIMPLE_WP_FRAGMENT = "SIMPLE_WP_FRAGMENT";
    private final static String SETTINGS_WP_FRAGMENT = "SETTINGS_WP_FRAGMENT";

    final ArrayList<WpFragmentInfo> mWpFragmentsInfo = new ArrayList<>(Arrays.<WpFragmentInfo>asList(
            new WpFragmentInfo(R.layout.fragment_wp_hello, SIMPLE_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_about_app, SIMPLE_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_theme_choice, SETTINGS_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_density_choice, SETTINGS_WP_FRAGMENT)
    ));

    private class WpFragmentInfo{
        final Integer wpFragmentLayoutId;
        final String wpFragmentType;

        WpFragmentInfo(Integer wpFragmentLayoutId, String wpFragmentType){
            this.wpFragmentLayoutId = wpFragmentLayoutId;
            this.wpFragmentType = wpFragmentType;
        }
    }

    int mCurrentFragmentIndex;
    WpFragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme_Dark_NoActionBar);

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_welcome_page);

        mCurrentFragmentIndex = 0;
        final WpFragmentInfo wpFragmentInfo = mWpFragmentsInfo.get(mCurrentFragmentIndex);
        lastFragment = replaceWpFragmentBy(wpFragmentInfo);

        final View nextButtton = findViewById(R.id.button_next);
        nextButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("nextButtton", String.valueOf(mCurrentFragmentIndex));
                if(mCurrentFragmentIndex < mWpFragmentsInfo.size() - 1){
                    mCurrentFragmentIndex++;
                    final WpFragmentInfo wpFragmentInfo = mWpFragmentsInfo.get(mCurrentFragmentIndex);
                    lastFragment = replaceWpFragmentBy(wpFragmentInfo);
                } else {
                    //TODO пределать, без ремува, новая активити создается раньше, чем преференсы сохраняются
                    removeWpFragment(lastFragment);
                    //mCurrentFragmentIndex = -1;
                    final Intent intent = new Intent();
                    intent.setClass(v.getContext(), LauncherActivity.class);
                    startActivity(intent);
                }
            }
        });

        checkForUpdates();
    }

    private WpFragment replaceWpFragmentBy(WpFragmentInfo wpFragmentInfo){
        final WpFragment currWpFragment = wpFragmentWithId(wpFragmentInfo);
        getFragmentManager().beginTransaction()
                .replace(R.id.wp_fragment_place, currWpFragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        return currWpFragment;
    }

    private void removeWpFragment(WpFragment wpFragment){
        getFragmentManager().beginTransaction()
                .remove(wpFragment)
                .addToBackStack(null)
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private WpFragment wpFragmentWithId(WpFragmentInfo wpFragmentInfo){
        final Bundle bundle = new Bundle();
        bundle.putInt(SimpleWpFragment.FRAGMENT_LAYOUT_ID, wpFragmentInfo.wpFragmentLayoutId);
        final WpFragment wpFragment;
        if(wpFragmentInfo.wpFragmentType.equals(SIMPLE_WP_FRAGMENT)){
            wpFragment = new SimpleWpFragment();
        }else{
            wpFragment = new SettingsWpFragment();
        }
        wpFragment.setArguments(bundle);
        return wpFragment;
    }

    public void onRadioClick(View view){
        final RadioButton thisRadioButton, anotherRadioButton;
        if(view.getId() == R.id.layout_radio_one
                || view.getId() == R.id.radio_one){
            thisRadioButton = findViewById(R.id.radio_one);
            anotherRadioButton = findViewById(R.id.radio_two);
        } else {
            thisRadioButton = findViewById(R.id.radio_two);
            anotherRadioButton= findViewById(R.id.radio_one);
        }
        thisRadioButton.setChecked(true);
        anotherRadioButton.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // ... your own onResume implementation
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }
}
