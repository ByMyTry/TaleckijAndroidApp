package com.taleckij_anton.taleckijapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

/**
 * Created by Lenovo on 31.01.2018.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_launcher);
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FloatingActionButton fab = getActivity().findViewById(R.id.fab_launcher);
        fab.setVisibility(View.VISIBLE);
    }
}
