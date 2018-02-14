package com.taleckij_anton.taleckijapp;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.taleckij_anton.taleckijapp.background_images.ImageLoaderService;
import com.taleckij_anton.taleckijapp.background_images.ImageSaver;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppsFragment;
import com.taleckij_anton.taleckijapp.launcher.recycler_training.LauncherRecyclerFragment;
import com.taleckij_anton.taleckijapp.launcher.SettingsFragment;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends AppCompatActivity{
    public static final String LAUNCH_FROM_WELCOME_PAGE = "LAUNCH_FROM_WELCOME_PAGE";
    public static final String LAUNCH_FROM_PROFILE = "LAUNCH_FROM_PROFILE";

    private static int sCurrentMenuItemId = -1;
    private final String CURRENT_MENU_ITEM_ID ="CURRENT_MENU_ITEM_ID";
    private final String CHANGE_THEME_FROM_SETTINGS = "CHANGE_THEME_FROM_SETTINGS";

    private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener(){
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
                    final String THEME_PREF_KEY = getResources().getString(R.string.theme_preference_key);
                    if(THEME_PREF_KEY.equals(key)){
                        LauncherActivity.this.finish();
                        final Intent intent = LauncherActivity.this.getIntent();
                        intent.putExtra(CHANGE_THEME_FROM_SETTINGS,true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(CURRENT_MENU_ITEM_ID, sCurrentMenuItemId);
                        LauncherActivity.this.startActivity(intent);
                    }
                }
            };

    private final BroadcastReceiver mUpdateImageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE.equals(action)) {
                final String imageName = intent.getStringExtra(ImageLoaderService.BROADCAST_PARAM_IMAGE);
                if(null == imageName){
                    final Bitmap bitmap = ImageSaver.getInstance().loadImage(getApplicationContext());
                    setDrawable(bitmap);
                } else {
                    if (TextUtils.isEmpty(imageName) == false
                            && LauncherActivity.class.getSimpleName().equals(imageName)) {
                        final Bitmap bitmap = ImageSaver.getInstance()
                                .loadImage(getApplicationContext(), imageName);
                        setDrawable(bitmap);
                    }
                }
            }
        }

        private void setDrawable(final Bitmap bitmap){
            final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            LauncherActivity.this.setDrawable(drawable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        launchWelcomePageIfNecessary();

        if(isDarkTheme()) {
            //getApplication().setTheme(R.style.AppTheme_Dark);
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_launcher);

        final Toolbar toolbar = findViewById(R.id.launcher_toolbar);
        setSupportActionBar(toolbar);
        final DrawerLayout drawerLayout = findViewById(R.id.launcher);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.launcher_drawer_open_desc, R.string.launcher_drawer_close_desc
        ){
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
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);

        final View myPhotoHeaderNavView = navigationView
                .getHeaderView(0).findViewById(R.id.nav_header_my_photo);
        myPhotoHeaderNavView.setOnClickListener(onHeaderPhotoClickListener);

        replaceFragment(savedInstanceState);
        //replaceRecyclerFragment(LauncherRecyclerFragment.GRID);

        checkForUpdates();
    }

    private void launchWelcomePageIfNecessary(){
        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
        final String layoutPrefKey = getResources().getString(R.string.compact_layout_preference_key);
        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
        if(!thisIsNotFirstRunning(themePrefKey, layoutPrefKey)
                || (launchWpOncePrefIsTrue(launchWpPrefKey)
                 && !getIntent().getBooleanExtra(CHANGE_THEME_FROM_SETTINGS,false)
                && !getIntent().getBooleanExtra(LAUNCH_FROM_WELCOME_PAGE,false)
                && !getIntent().getBooleanExtra(LAUNCH_FROM_PROFILE, false))
                ){
            Intent intent = new Intent();
            intent.putExtra(WelcomePageActivity.LAUNCH_FROM_LAUNCHER, true);
            intent.setClass(this, WelcomePageActivity.class);
            startActivity(intent);
        }
    }

    private boolean thisIsNotFirstRunning(String themePrefKey, String layoutPrefKey){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.contains(themePrefKey) && sharedPreferences.contains(layoutPrefKey);
    }

    private boolean launchWpOncePrefIsTrue(String launchWpPrefKey){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final boolean launchWpOnce = sharedPreferences.getBoolean(launchWpPrefKey, false);
        return launchWpOnce;
    }

    public boolean isDarkTheme(){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
        return sharedPreferences.getBoolean(themePrefKey, false);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.launcher);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private final NavigationView.OnNavigationItemSelectedListener
            onNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
                private DrawerLayout mDrawerLayout;

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int menuItemId = item.getItemId();
                    //replaceAppsFragment();
                    if(menuItemId!= sCurrentMenuItemId) {
                        sCurrentMenuItemId = menuItemId;
                        replaceFragmentByItemId(menuItemId);
                    }

                    if(mDrawerLayout == null){
                        mDrawerLayout = findViewById(R.id.launcher);
                    }
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    private void replaceFragmentByItemId(int menuItemId){
        if(menuItemId == R.id.launcher_grid_menu_item) {
            replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
            YandexMetrica.reportEvent(MetricaAppEvents.AppsGridOpen);
        } else if(menuItemId == R.id.launcher_linear_menu_item) {
            replaceAppsFragment(AppsFragment.APPS_LINEAR_LAYOUT);
            YandexMetrica.reportEvent(MetricaAppEvents.AppsLinearOpen);
        /*}else if (menuItemId == R.id.grid_layout_menu_item){
            replaceRecyclerFragment(LauncherRecyclerFragment.GRID);
        } else if(menuItemId == R.id.linear_layout_menu_item){
            replaceRecyclerFragment(LauncherRecyclerFragment.LINEAR);*/
        } else if(menuItemId == R.id.settings_menu_item) {
            replaceSettingsFragment();
            YandexMetrica.reportEvent(MetricaAppEvents.AppsSettingsOpen);
        }
    }

    private final View.OnClickListener
            onHeaderPhotoClickListener = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(v.getContext(), AboutMeActivity.class);
                    startActivity(intent);
                }
            };

    private void replaceAppsFragment(String layoutType){
        AppsFragment appsFragment = AppsFragment.getInstance(layoutType);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, appsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    /*private void replaceRecyclerFragment(String fragmentType){
       final LauncherRecyclerFragment launcherRecyclerFragment
               = LauncherRecyclerFragment.getInstance(fragmentType);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, launcherRecyclerFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }*/

    private void replaceSettingsFragment(){
        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, new SettingsFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void replaceFragment(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            int currentMenuItemId = savedInstanceState.getInt(CURRENT_MENU_ITEM_ID, sCurrentMenuItemId);
            if(currentMenuItemId != -1){
                sCurrentMenuItemId = currentMenuItemId;
                replaceFragmentByItemId(currentMenuItemId);
            } else {
                replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
            }
        } else if(getIntent() != null){
            int currentMenuItemId = getIntent().getIntExtra(CURRENT_MENU_ITEM_ID, sCurrentMenuItemId);
            if(currentMenuItemId != -1){
                getIntent().putExtra(CURRENT_MENU_ITEM_ID, -1);
                sCurrentMenuItemId = currentMenuItemId;
                replaceFragmentByItemId(currentMenuItemId);
            } else {
                replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
            }
        } else {
            replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
        }
    }

    private void backgroundImageProcess(){
        ImageLoaderService.enqueueWork(this, ImageLoaderService.ACTION_LOAD_IMAGE,
                this.getClass().getSimpleName());
        //TODO запустить аларм менеджер на очищение ImageSaver и запуск IML
    }

    private void setDrawable(Drawable drawable) {
        findViewById(R.id.launcher).setBackground(drawable);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findViewById(R.id.launcher).setBackground(drawable);
        } else {
            findViewById(R.id.launcher).setBackgroundDrawable(drawable);
        }*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (sCurrentMenuItemId != -1) {
            outState.putInt(CURRENT_MENU_ITEM_ID, sCurrentMenuItemId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        registerReceiver(mUpdateImageBroadcastReceiver,
                new IntentFilter(ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE));
        backgroundImageProcess();

        checkForCrashes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        unregisterReceiver(mUpdateImageBroadcastReceiver);

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

