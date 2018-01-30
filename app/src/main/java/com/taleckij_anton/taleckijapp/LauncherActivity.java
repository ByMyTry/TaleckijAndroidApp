package com.taleckij_anton.taleckijapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {

     /*IconsCountSetting/{
        FOR_PORTRAIT, FOR_LANDSCAPE;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        final Intent intent = getIntent();
        //final Integer iconsInRowPort = intent.getIntExtra(IconsCountSetting.FOR_PORTRAIT,);
    }
}
