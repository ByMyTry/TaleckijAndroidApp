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
    public final View.OnClickListener onFinishButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openLauncher(v.getContext());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

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

    private void openLauncher(final Context context){
        final Intent intent = new Intent();
        intent.setClass(context, LauncherActivity.class);
        startActivity(intent);
    }
}
