package com.taleckij_anton.taleckijapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.taleckij_anton.taleckijapp.background_images.ImageLoaderService;
import com.taleckij_anton.taleckijapp.background_images.ImageSaver;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import java.util.List;


public class AboutMeActivity extends AppCompatActivity {

    private final static String GITHUB_URL = "https://github.com/ByMyTry/TaleckijAndroidApp";

    private final BroadcastReceiver mUpdateImageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE.equals(action)) {
                final String className = AboutMeActivity.class.getSimpleName();
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
            } else if(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE.equals(action)){
                List<String> imageNames = ImageSaver.getInstance().clear(context);
                for(String imageName : imageNames) {
                    ImageLoaderService.enqueueWork(context, ImageLoaderService.ACTION_LOAD_IMAGE,
                            imageName);
                }
            }
        }

        private void setDrawable(final Bitmap bitmap){
            final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            AboutMeActivity.this.setDrawable(drawable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
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

    private void backgroundImageProcess(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String diffBackImagePrefKey =
                getResources().getString(R.string.diff_background_image_pref_key);
        Boolean useDiffImages = sharedPreferences.getBoolean(diffBackImagePrefKey, false);
        String name = null;
        if(useDiffImages) {
            name = this.getClass().getSimpleName();
        }
        ImageLoaderService.enqueueWork(this, ImageLoaderService.ACTION_LOAD_IMAGE, name);
    }

    private void setDrawable(Drawable drawable) {
        findViewById(R.id.profile_content).setBackground(drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE);
        intentFilter.addAction(ImageLoaderService.BROADCAST_ACTION_UPDATE_CACHE);
        registerReceiver(mUpdateImageBroadcastReceiver, intentFilter);
        backgroundImageProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUpdateImageBroadcastReceiver);
    }
}
