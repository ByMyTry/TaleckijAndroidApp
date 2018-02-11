package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;


public class AboutMeActivity extends AppCompatActivity {

    private final static String GITHUB_URL = "https://github.com/ByMyTry/TaleckijAndroidApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

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
       /* TextView textView = findViewById(R.id.my_github_link);
        makeGithubLink(textView, GITHUB_URL);

        CardView cardView = findViewById(R.id.my_photo_card);
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Intent intent = new Intent();
                intent.setClass(v.getContext(), WelcomePageActivity.class);
                startActivity(intent);
                return true;
            }
        });*/
    }

    public boolean isDarkTheme(){
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        final String themePrefKey = getResources().getString(R.string.theme_preference_key);
        return sharedPreferences.getBoolean(themePrefKey, false);
    }

    private void makeGithubLink(TextView textView, String github_url){
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String locale_my = getResources().getString(R.string.locale_my);
        String text = "<a href='" + github_url + "'>" + locale_my + " github</a>";
        textView.setText(Html.fromHtml(text));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "LOLKEKCHEBURKEK", Toast.LENGTH_SHORT).show();
        //super.onBackPressed();
    }
}
