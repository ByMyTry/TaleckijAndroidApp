package com.taleckij_anton.taleckijapp.welcome_page;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lenovo on 01.02.2018.
 */

public class WpFragment extends Fragment {
    public static final String FRAGMENT_LAYOUT_ID = "FRAGMENT_LAYOUT_ID";
    protected int fragmentLayoutId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragmentLayoutId = getArguments().getInt(FRAGMENT_LAYOUT_ID);
        final View view = inflater.inflate(fragmentLayoutId, container, false);
        return view;
    }
}
