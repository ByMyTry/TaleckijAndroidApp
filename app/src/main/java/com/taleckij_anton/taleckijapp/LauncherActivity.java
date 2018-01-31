package com.taleckij_anton.taleckijapp;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class LauncherActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        navigationView.setNavigationItemSelectedListener(this);

        replaceFragment(LauncherGridFragment.GRID);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();

        if(menuItemId == R.id.grid_layout_menu_item){
            replaceFragment(LauncherGridFragment.GRID);
        } else if(menuItemId == R.id.linear_layout_menu_item){
            replaceFragment(LauncherGridFragment.LINEAR);
        } else if(menuItemId == R.id.settings_menu_item) {
            return false;
        }

        DrawerLayout drawerLayout = findViewById(R.id.launcher);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(String fragmentType){
        final LauncherGridFragment launcherFragment = new LauncherGridFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(LauncherGridFragment.LAYOUT_TYPE, fragmentType);
        launcherFragment.setArguments(arguments);

        getFragmentManager().beginTransaction()
                .replace(R.id.list_fragment_place, launcherFragment)
                .commit();
    }
}
