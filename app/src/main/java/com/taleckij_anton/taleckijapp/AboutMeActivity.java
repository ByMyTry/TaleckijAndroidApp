package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;


public class AboutMeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        final Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent();
                intent.putExtra(LauncherActivity.LAUNCH_FROM_PROFILE, true);
                intent.setClass(v.getContext(), LauncherActivity.class);
                startActivity(intent);
            }
        });

        YandexMetrica.reportEvent(MetricaAppEvents.ProfileOpen);
    }

    @Nullable
    @Override
    protected View getBackView() {
        return findViewById(R.id.profile_content);
    }
}
