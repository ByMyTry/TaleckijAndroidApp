package com.taleckij_anton.taleckijapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.taleckij_anton.taleckijapp.background_images.ImageLoaderService;
import com.taleckij_anton.taleckijapp.background_images.ImageSaver;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import java.util.List;

/**
 * Created by Lenovo on 03.03.2018.
 */

public class BaseActivity extends AppCompatActivity {
    private long mCurrentViewPageIndex = -1;

    protected long getCurrentViewPageIndex() {
        return mCurrentViewPageIndex;
    }

    protected void setCurrentViewPageIndex(long currentViewPageIndex) {
        this.mCurrentViewPageIndex = currentViewPageIndex;
    }

    protected final String CURRENT_VIEW_PAGE_INDEX ="CURRENT_VIEW_PAGE_INDEX";

    private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener(){
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
                    final String THEME_PREF_KEY = getResources().getString(R.string.theme_preference_key);
                    final String UPDATE_CACHE_INTERVAL_PREF_KEY = getResources().getString(R.string.update_cache_interval_pref_key);
                    final String DIFF_BACK_IMAGE_PREF_KEY = getResources().getString(R.string.diff_background_image_pref_key);
                    final String LAYOUT_TYPE_PREF_KEY = getResources().getString(R.string.compact_layout_preference_key);
                    final String SORT_TYPE_APPS_KEY = getResources().getString(R.string.sorttype_apps_pref_key);
                    final String SHOW_WP_NEXT_TIME_PREF_KEY = getResources().getString(R.string.launch_wp_pref_key);
                    if(THEME_PREF_KEY.equals(key)){
                        reloadActivity();

                        YandexMetrica.reportEvent(MetricaAppEvents.ChangeTheme);
                    } else if(UPDATE_CACHE_INTERVAL_PREF_KEY.equals(key)) {
                        String currentIntervalMin = String.valueOf(ImageLoaderService.getUpdateCacheInterval());
                        Long interval = Long.valueOf(sharedPreferences.getString(key, currentIntervalMin));
                        if(interval == 0) {
                            sharedPreferences.edit()
                                    .putString(key, currentIntervalMin)
//                                    .commit();
                                    .apply();
                            //replaceFragmentByItemId(mCurrentMenuItemId);
                            openViewPage(getCurrentViewPageIndex());
                            sendBroadcast(new Intent(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE));

                            YandexMetrica.reportEvent(MetricaAppEvents.UpdateBackImgsCacheNowOptionChanged);
                        } else {
                            ImageLoaderService.setUpdateCacheInterval(interval);

                            YandexMetrica.reportEvent(MetricaAppEvents.ChangeUpdateCacheInterval);
                        }

                    } else if(DIFF_BACK_IMAGE_PREF_KEY.equals(key)){
                        ImageSaver.getInstance().clear(BaseActivity.this);
                        reloadActivity();

                        YandexMetrica.reportEvent(MetricaAppEvents.DiffBackImagesOptionChanged);
                    }else if(LAYOUT_TYPE_PREF_KEY.equals(key)){
                        YandexMetrica.reportEvent(MetricaAppEvents.ChangeLayoutType);
                    }else if(SORT_TYPE_APPS_KEY.equals(key)){
                        YandexMetrica.reportEvent(MetricaAppEvents.ChangeSortType);
                    }else if(SHOW_WP_NEXT_TIME_PREF_KEY.equals(key)){
                        YandexMetrica.reportEvent(MetricaAppEvents.ChangeShowWpNextTime);
                    }
                }

                private void reloadActivity(){
                    final Intent intent = BaseActivity.this.getIntent();
                    BaseActivity.this.finish();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(CURRENT_VIEW_PAGE_INDEX, getCurrentViewPageIndex());
                    BaseActivity.this.startActivity(intent);
                }
            };

    private final BroadcastReceiver mUpdateImageBroadcastReceiver = new BroadcastReceiver() {
        private Drawable defaultBackDraw;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if(ImageLoaderService.BROADCAST_ACTION_SET_TEMP_IMAGE.equals(action)) {
                defaultBackDraw = getResources().getDrawable(R.drawable.back_image_default, getTheme());
                setDrawable(((BitmapDrawable) defaultBackDraw).getBitmap());

            } else if(ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE.equals(action)) {
                final String className = getCurrentActivityName();
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

                YandexMetrica.reportEvent(MetricaAppEvents.SetBackgroundImage);
            } else if(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE.equals(action)){
                final List<String> imageNames = ImageSaver.getInstance().clear(context);
                for(String imageName : imageNames) {
                    ImageLoaderService.enqueueWork(context, ImageLoaderService.ACTION_LOAD_IMAGE,
                            imageName);
                }

                YandexMetrica.reportEvent(MetricaAppEvents.UpdateBackImagesCache);
            }
        }

        private void setDrawable(final Bitmap bitmap){
            final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            setActivityBack(drawable);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setCurrentTheme();
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        //TODO сделать отдельный Action на Image Service где, если не закешина картинка, ставить дефолт или то, что есть на диске
//        //default back
//        setActivityBack(getResources().getDrawable(R.drawable.back_image_default, getTheme()));
//    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE);
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_SET_TEMP_IMAGE);
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE);
        registerReceiver(mUpdateImageBroadcastReceiver, intentFilter);
        backgroundImageProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        unregisterReceiver(mUpdateImageBroadcastReceiver);
    }

    private String getCurrentActivityName() {
        return getClass().getSimpleName();
    }

    private void setCurrentTheme() {
        if(isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    private boolean isDarkTheme(){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
        return sharedPreferences.getBoolean(themePrefKey, false);
    }

    private void backgroundImageProcess(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String diffBackImagePrefKey =
                getResources().getString(R.string.diff_background_image_pref_key);
        Boolean useDiffImages = sharedPreferences.getBoolean(diffBackImagePrefKey, false);
        String name = null;
        if (useDiffImages) {
            name = getCurrentActivityName();
        }
        ImageLoaderService.enqueueWork(this, ImageLoaderService.ACTION_LOAD_IMAGE, name);
    }

    private void setActivityBack(Drawable drawable) {
        final View backView = getBackView();
        if(backView != null) {
            backView.setBackground(drawable);
        }
    }

    @Nullable
    protected View getBackView(){
        return null;
    }

    protected void openViewPage(long index){

    }
}
