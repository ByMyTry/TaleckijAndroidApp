package com.taleckij_anton.taleckijapp;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.crashlytics.android.Crashlytics;
import com.taleckij_anton.taleckijapp.background_images.ImageLoaderService;
import com.taleckij_anton.taleckijapp.background_images.ImageSaver;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.taleckij_anton.taleckijapp.welcome_page.SettingsWpFragment;
import com.taleckij_anton.taleckijapp.welcome_page.SimpleWpFragment;
import com.taleckij_anton.taleckijapp.welcome_page.WpFragment;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class WelcomePageActivity extends AppCompatActivity {
    public final static String LAUNCH_FROM_LAUNCHER = "LAUNCH_FROM_LAUNCHER";

    private final static String SIMPLE_WP_FRAGMENT = "SIMPLE_WP_FRAGMENT";
    private final static String SETTINGS_WP_FRAGMENT = "SETTINGS_WP_FRAGMENT";

    private final BroadcastReceiver mUpdateImageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE.equals(action)) {
                //final String imageName = intent.getStringExtra(ImageLoaderService.BROADCAST_PARAM_IMAGE);
                final String className = WelcomePageActivity.class.getSimpleName();
                final Boolean hasImageName = intent.getBooleanExtra(className, false);
                final Boolean hasDefaultImageName =
                        intent.getBooleanExtra(ImageSaver.DEFAULT_IMAGE_NAME, false);
                final String imageName = hasImageName ? className:
                        hasDefaultImageName ? ImageSaver.DEFAULT_IMAGE_NAME: null;
                if(TextUtils.isEmpty(imageName) == false){
                    final Bitmap bitmap = ImageSaver.getInstance()
                            .loadImage(getApplicationContext(), imageName);
                    setDrawable(bitmap);
                }
                /*if(null == imageName){
                    final Bitmap bitmap = ImageSaver.getInstance().loadImage(getApplicationContext());
                    setDrawable(bitmap);
                } else {
                    if (TextUtils.isEmpty(imageName) == false
                            && WelcomePageActivity.class.getSimpleName().equals(imageName)) {
                        final Bitmap bitmap = ImageSaver.getInstance()
                                .loadImage(getApplicationContext(), imageName);
                        setDrawable(bitmap);
                    }
                }*/
            } else if(ImageLoaderService.ACTION_UPDATE_CACHE.equals(action)){
                List<String> imageNames = ImageSaver.getInstance().clear(context);
                for(String imageName : imageNames) {
                    ImageLoaderService.enqueueWork(context, ImageLoaderService.ACTION_LOAD_IMAGE,
                            imageName);
                }
            }
        }

        private void setDrawable(final Bitmap bitmap){
            final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            WelcomePageActivity.this.setDrawable(drawable);
        }
    };

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
        if(isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
        ifWpOnceTrueSetFalse(launchWpPrefKey);

        mCurrentFragmentIndex = 0;
        final WpFragmentInfo wpFragmentInfo = mWpFragmentsInfo.get(mCurrentFragmentIndex);
        lastFragment = replaceWpFragmentBy(wpFragmentInfo);

        final View nextButtton = findViewById(R.id.button_next);
        nextButtton.setOnClickListener(mNextButtonOnClick);

        YandexMetrica.reportEvent(MetricaAppEvents.WelcomePageOpen);
    }

    public boolean isDarkTheme(){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
        return sharedPreferences.getBoolean(themePrefKey, false);
    }

    private void ifWpOnceTrueSetFalse(String launchWpPrefKey){
        if(getIntent().getBooleanExtra(LAUNCH_FROM_LAUNCHER, false)) {
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            final boolean launchWpOnce = sharedPreferences.getBoolean(launchWpPrefKey, false);
            if (launchWpOnce) {
                sharedPreferences.edit()
                        .putBoolean(launchWpPrefKey, false)
                        .apply();
            }
        }
    }

    private final View.OnClickListener
            mNextButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Log.i("nextButtton", String.valueOf(mCurrentFragmentIndex));
            if(mCurrentFragmentIndex < mWpFragmentsInfo.size() - 1){
                mCurrentFragmentIndex++;
                final WpFragmentInfo wpFragmentInfo = mWpFragmentsInfo.get(mCurrentFragmentIndex);
                lastFragment = replaceWpFragmentBy(wpFragmentInfo);
            } else {
                //TODO пределать, без ремува; новая активити создается раньше, чем преференсы сохраняются
                removeWpFragment(lastFragment);
                //mCurrentFragmentIndex = -1;
                final Intent intent = new Intent();
                intent.putExtra(LauncherActivity.LAUNCH_FROM_WELCOME_PAGE, true);
                intent.setClass(v.getContext(), LauncherActivity.class);
                startActivity(intent);
            }
        }
    };

    private WpFragment replaceWpFragmentBy(WpFragmentInfo wpFragmentInfo){
        final WpFragment currWpFragment = wpFragmentWithId(wpFragmentInfo);
        getFragmentManager().beginTransaction()
                .replace(R.id.wp_fragment_place, currWpFragment)
                //.addToBackStack(null)
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
        final WpFragment wpFragment;
        if(wpFragmentInfo.wpFragmentType.equals(SIMPLE_WP_FRAGMENT)){
            wpFragment = SimpleWpFragment.getInstance(wpFragmentInfo.wpFragmentLayoutId);
        }else{
            wpFragment = SettingsWpFragment.getInstance(wpFragmentInfo.wpFragmentLayoutId);
        }
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

    private void backgroundImageProcess(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String diffBackImagePrefKey =
                getResources().getString(R.string.diff_background_image_pref_key);
        Boolean useDiffImages = sharedPreferences.getBoolean(diffBackImagePrefKey, false);
        String name = null;
        if(useDiffImages) {
            name = this.getClass().getSimpleName();
        }
        ImageLoaderService.enqueueWork(this, ImageLoaderService.ACTION_LOAD_IMAGE, name);
    }

    private void setDrawable(Drawable drawable) {
        findViewById(R.id.wp_fragment_place).setBackground(drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE);
        intentFilter.addAction(ImageLoaderService.ACTION_UPDATE_CACHE);
        registerReceiver(mUpdateImageBroadcastReceiver, intentFilter);
        backgroundImageProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUpdateImageBroadcastReceiver);
    }
}
