package com.taleckij_anton.taleckijapp;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.crashlytics.android.Crashlytics;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class WelcomePageActivity extends AppCompatActivity {

    private final ArrayList<Integer> mWpFragmentsLayoutsIds = new ArrayList<>(Arrays.<Integer>asList(
            R.layout.wp_hello_fragment,
            R.layout.wp_about_app_fragment,
            R.layout.wp_theme_color_choice,
            R.layout.wp_views_count_choice
    ));

    int mCurrentFragmentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_welcome_page);
        getSupportActionBar().hide();

        mCurrentFragmentIndex = 0;
        int currentFragmentLayoutId = mWpFragmentsLayoutsIds.get(mCurrentFragmentIndex);
        replaceWpFragmentBy(currentFragmentLayoutId);

        final View nextButtton = findViewById(R.id.button_next);
        nextButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(mCurrentFragmentIndex < mWpFragmentsLayoutsIds.size() - 1){
                    mCurrentFragmentIndex++;
                    int currentFragmentLayoutId = mWpFragmentsLayoutsIds.get(mCurrentFragmentIndex);
                    replaceWpFragmentBy(currentFragmentLayoutId);
                } else {
                    mCurrentFragmentIndex = -1;
                    final Intent intent = new Intent();
                    intent.setClass(v.getContext(), LauncherActivity.class);
                    startActivity(intent);
                }
            }
        });

        checkForUpdates();
    }

    private void replaceWpFragmentBy(int fragmentlayoutId){
        getFragmentManager().beginTransaction()
                .replace(R.id.wp_fragment_place, simpleFragmentWithId(fragmentlayoutId))
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private SimpleFragment simpleFragmentWithId(int fragmentlayoutId){
        Bundle bundle = new Bundle();
        bundle.putInt(SimpleFragment.FRAGMENT_LAYOUT_ID, fragmentlayoutId);
        SimpleFragment simpleFragment = new SimpleFragment();
        simpleFragment.setArguments(bundle);
        return simpleFragment;
    }

    public void onRadioClick(View view){
        /*final View.OnClickListener viewListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioButton thisRadioButton, anotherRadioButton;
                if(view.getId() == R.id.layout_radio_one){
                    thisRadioButton = findViewById(R.id.radio_one);
                    anotherRadioButton = findViewById(R.id.radio_two);
                } else {
                    thisRadioButton = findViewById(R.id.radio_two);
                    anotherRadioButton= findViewById(R.id.radio_one);
                }
                thisRadioButton.setChecked(true);
                anotherRadioButton.setChecked(false);
            }
        };
        viewListener.onClick(view);*/
        final RadioButton thisRadioButton, anotherRadioButton;
        if(view.getId() == R.id.layout_radio_one){
            thisRadioButton = findViewById(R.id.radio_one);
            anotherRadioButton = findViewById(R.id.radio_two);
        } else {
            thisRadioButton = findViewById(R.id.radio_two);
            anotherRadioButton= findViewById(R.id.radio_one);
        }
        thisRadioButton.setChecked(true);
        anotherRadioButton.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // ... your own onResume implementation
        checkForCrashes();
    }

    @Override
    public void onPause() {
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
