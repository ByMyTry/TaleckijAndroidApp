package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.taleckij_anton.taleckijapp.background_images.ImageLoaderService;
import com.taleckij_anton.taleckijapp.background_images.ImageSaver;
import com.taleckij_anton.taleckijapp.launcher.AppsVpFragment;
import com.taleckij_anton.taleckijapp.launcher.SettingsFragment;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends BaseActivity{
//    private static long mCurrentMenuItemId = -1;
    private long mCurrentMenuItemId = -1;
    private final String CURRENT_MENU_ITEM_ID ="CURRENT_MENU_ITEM_ID";

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
                                    .commit();
                            replaceFragmentByItemId(mCurrentMenuItemId);
                            sendBroadcast(new Intent(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE));

                            YandexMetrica.reportEvent(MetricaAppEvents.UpdateBackImgsCacheNowOptionChanged);
                        } else {
                            ImageLoaderService.setUpdateCacheInterval(interval);

                            YandexMetrica.reportEvent(MetricaAppEvents.ChangeUpdateCacheInterval);
                        }

                    } else if(DIFF_BACK_IMAGE_PREF_KEY.equals(key)){
                        ImageSaver.getInstance().clear(LauncherActivity.this);
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
                    final Intent intent = LauncherActivity.this.getIntent();
                    LauncherActivity.this.finish();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(CURRENT_MENU_ITEM_ID, mCurrentMenuItemId);
                    Log.i("reloadActivity", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + mCurrentMenuItemId);
                    LauncherActivity.this.startActivity(intent);
                }
            };

    private final NavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        private DrawerLayout mDrawerLayout;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int menuItemId = item.getItemId();
            if (menuItemId != mCurrentMenuItemId) {
                mCurrentMenuItemId = menuItemId;
                replaceFragmentByItemId(menuItemId);
            }

            if (mDrawerLayout == null) {
                mDrawerLayout = findViewById(R.id.launcher);
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launcher);

        final Toolbar toolbar = findViewById(R.id.launcher_toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawerLayout = findViewById(R.id.launcher);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.launcher_drawer_open_desc, R.string.launcher_drawer_close_desc
        ) {
            //http://thetechnocafe.com/slide-content-to-side-in-drawer-layout-android/
            /*private float scaleFactor = 6f;
            private FrameLayout content = findViewById(R.id.list_fragment_place);

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                if(ViewCompat.getLayoutDirection(drawerView) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    content.setTranslationX(slideX);
                } else{
                    content.setTranslationX(-slideX);
                }
                content.setScaleX(1 - (slideOffset / scaleFactor));
                content.setScaleY(1 - (slideOffset / scaleFactor));
            }*/
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.launcher_nav_view);
        navigationView.setNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final View myPhotoHeaderNavView = navigationView
                .getHeaderView(0)
                .findViewById(R.id.nav_header_my_photo);
        myPhotoHeaderNavView.setOnClickListener(onHeaderPhotoClickListener);

        replaceFragment(savedInstanceState);

        checkForUpdates();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.launcher);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private final View.OnClickListener onHeaderPhotoClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(v.getContext(), AboutMeActivity.class);
            startActivity(intent);
        }
    };

    private void replaceFragmentByItemId(long menuItemId){
        if(menuItemId == R.id.launcher_desk_menu_item) {
            replaceAppsVpFragment();
        } /*else if (menuItemId == R.id.grid_layout_menu_item){
            replaceRecyclerFragment(LauncherRecyclerFragment.GRID);
        } else if(menuItemId == R.id.linear_layout_menu_item){
            replaceRecyclerFragment(LauncherRecyclerFragment.LINEAR);
        }*/ else if(menuItemId == R.id.settings_menu_item) {
            replaceSettingsFragment();

            YandexMetrica.reportEvent(MetricaAppEvents.AppsSettingsOpen);
        }
    }

    private AppsVpFragment replaceAppsVpFragment() {
        AppsVpFragment appsVpFragment = new AppsVpFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launcher_main_fragment_place, appsVpFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        return appsVpFragment;
    }

    /*private void replaceRecyclerFragment(String fragmentType){
       final LauncherRecyclerFragment launcherRecyclerFragment
               = LauncherRecyclerFragment.getInstance(fragmentType);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, launcherRecyclerFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }*/

    private SettingsFragment replaceSettingsFragment(){
        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.launcher_main_fragment_place, settingsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        return settingsFragment;
    }

    private void replaceFragment(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long currentMenuItemId = savedInstanceState.getLong(CURRENT_MENU_ITEM_ID, -1);
            Log.i("savedInstanceState", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + currentMenuItemId);
            if (currentMenuItemId != -1) {
                mCurrentMenuItemId = currentMenuItemId;
                replaceFragmentByItemId(currentMenuItemId);
            } else {
                replaceAppsVpFragment();
                //replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
            }
        } else if(getIntent() != null) {
            long currentMenuItemId = getIntent().getLongExtra(CURRENT_MENU_ITEM_ID, -1);
            Log.i("getIntent", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + currentMenuItemId);
            if (currentMenuItemId != -1) {
                getIntent().putExtra(CURRENT_MENU_ITEM_ID, -1);
                mCurrentMenuItemId = currentMenuItemId;
                replaceFragmentByItemId(currentMenuItemId);
            } else {
                replaceAppsVpFragment();
                //replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
            }
        } else {
            replaceAppsVpFragment();
            //replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
        }
    }

    @Nullable
    @Override
    protected View getBackView() {
        return findViewById(R.id.launcher);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCurrentMenuItemId != -1) {
            Log.i("onSaveInstanceState", "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + mCurrentMenuItemId);
            outState.putLong(CURRENT_MENU_ITEM_ID, mCurrentMenuItemId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

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

