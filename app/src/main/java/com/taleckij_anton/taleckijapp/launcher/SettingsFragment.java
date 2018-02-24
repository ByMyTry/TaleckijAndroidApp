package com.taleckij_anton.taleckijapp.launcher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.taleckij_anton.taleckijapp.R;

/**
 * Created by Lenovo on 31.01.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preference, rootKey);
    }

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        //setFabVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //setFabVisibility(View.VISIBLE);
    }*/

    /*private void setFabVisibility(int visibility){
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_launcher);
        if(fab != null) {
            fab.setVisibility(visibility);
        }
    }*/
}
