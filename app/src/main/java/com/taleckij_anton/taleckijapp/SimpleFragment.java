package com.taleckij_anton.taleckijapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lenovo on 29.01.2018.
 */

public class SimpleFragment extends Fragment {
    public static final String FRAGMENT_LAYOUT_ID = "FRAGMENT_LAYOUT_ID";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        int fragment_layout_id = getArguments().getInt(FRAGMENT_LAYOUT_ID);
        final View view = inflater.inflate(fragment_layout_id, container, false);

        if(view.findViewById(R.id.usual_layout_land_text) != null){
            //TODO Либо здесь сделать корретировку ресурсов через подстановку пременных из R.integer, либо отдельная реализация для SettingFragment
        }

        return view;
    }
}
