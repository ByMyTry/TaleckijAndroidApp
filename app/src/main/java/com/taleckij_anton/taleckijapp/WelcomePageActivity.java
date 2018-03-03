package com.taleckij_anton.taleckijapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.taleckij_anton.taleckijapp.welcome_page.WpPageAdapter;
import com.yandex.metrica.YandexMetrica;

public class WelcomePageActivity extends BaseActivity {
    public final static String LAUNCH_FROM_LAUNCHER = "LAUNCH_FROM_LAUNCHER";
    public final View.OnClickListener onFinishButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finishWpAndOpenLauncher(v.getContext());
        }
    };

//    private final static String SIMPLE_WP_FRAGMENT = "SIMPLE_WP_FRAGMENT";
//    private final static String SETTINGS_WP_FRAGMENT = "SETTINGS_WP_FRAGMENT";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        ifWpOnceTrueSetFalse();

        final ViewPager wpViewPager = findViewById(R.id.wp_view_pager);
        wpViewPager.setAdapter(new WpPageAdapter(getSupportFragmentManager()));

        final TabLayout wpPageIndicator = findViewById(R.id.wp_pager_indicator);
        wpPageIndicator.setupWithViewPager(wpViewPager, true);

        YandexMetrica.reportEvent(MetricaAppEvents.WelcomePageOpen);
    }

    @Nullable
    @Override
    protected View getBackView() {
        return findViewById(R.id.wp_fragment_place);
    }

    private void ifWpOnceTrueSetFalse(){
        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
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

    private void finishWpAndOpenLauncher(final Context context){
        final Intent intent = new Intent();
        intent.putExtra(LauncherActivity.LAUNCH_FROM_WELCOME_PAGE, true);
        intent.setClass(context, LauncherActivity.class);
        startActivity(intent);
    }
}
