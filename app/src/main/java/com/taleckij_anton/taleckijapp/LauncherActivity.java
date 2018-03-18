package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.taleckij_anton.taleckijapp.launcher.AppsVpFragment;
import com.taleckij_anton.taleckijapp.launcher.SettingsFragment;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import io.fabric.sdk.android.Fabric;

public class LauncherActivity extends BaseActivity{

    private final NavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        private DrawerLayout mDrawerLayout;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int menuItemId = item.getItemId();
            if (menuItemId != getCurrentViewPageIndex()) {
                setCurrentViewPageIndex(menuItemId);
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
            long currentMenuItemId = savedInstanceState.getLong(CURRENT_VIEW_PAGE_INDEX, -1);
            if (currentMenuItemId != -1) {
                setCurrentViewPageIndex(currentMenuItemId);
                replaceFragmentByItemId(currentMenuItemId);
                return;
            }
        } else if(getIntent() != null) {
            long currentMenuItemId = getIntent().getLongExtra(CURRENT_VIEW_PAGE_INDEX, -1);
            if (currentMenuItemId != -1) {
                getIntent().putExtra(CURRENT_VIEW_PAGE_INDEX, -1);
                setCurrentViewPageIndex(currentMenuItemId);
                replaceFragmentByItemId(currentMenuItemId);
                return;
            }
        }
        replaceAppsVpFragment();
        //replaceAppsFragment(AppsFragment.APPS_GRID_LAYOUT);
    }

    @Nullable
    @Override
    protected View getBackView() {
        return findViewById(R.id.launcher);
    }

    @Override
    protected void openViewPage(long index) {
        replaceFragmentByItemId(index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (getCurrentViewPageIndex() != -1) {
            outState.putLong(CURRENT_VIEW_PAGE_INDEX, getCurrentViewPageIndex());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkForCrashes();
    }

    @Override
    protected void onPause() {
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

