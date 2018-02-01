package com.taleckij_anton.taleckijapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class LauncherActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Toolbar toolbar = findViewById(R.id.launcher_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.launcher);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.launcher_drawer_open_desc, R.string.launcher_drawer_close_desc
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.launcher_nav_view);
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);

        replaceRecyclerFragment(LauncherGridFragment.GRID);
    }

    public boolean isDarkTheme(){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    private final NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int menuItemId = item.getItemId();

                    if(menuItemId == R.id.grid_layout_menu_item){
                        replaceRecyclerFragment(LauncherGridFragment.GRID);
                    } else if(menuItemId == R.id.linear_layout_menu_item){
                        replaceRecyclerFragment(LauncherGridFragment.LINEAR);
                    } else if(menuItemId == R.id.settings_menu_item) {
                        replaceSettingsFragment();
                    }

                    DrawerLayout drawerLayout = findViewById(R.id.launcher);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    private void replaceRecyclerFragment(String fragmentType){
        final LauncherGridFragment launcherFragment = new LauncherGridFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(LauncherGridFragment.LAYOUT_TYPE, fragmentType);
        launcherFragment.setArguments(arguments);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, launcherFragment)
                .commit();
    }

    private void replaceSettingsFragment(){
        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener(){
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
                    final String themePrefKey = getResources().getString(R.string.theme_preference_key);
                    if(themePrefKey.equals(key)){
                        LauncherActivity.this.finish();
                        final Intent intent = LauncherActivity.this.getIntent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        LauncherActivity.this.startActivity(intent);
                    }
                }
            };
}

