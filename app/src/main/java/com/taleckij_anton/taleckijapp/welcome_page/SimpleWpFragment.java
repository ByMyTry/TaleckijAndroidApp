package com.taleckij_anton.taleckijapp.welcome_page;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lenovo on 29.01.2018.
 */

public class SimpleWpFragment extends WpFragment {
    /*public static final String FRAGMENT_LAYOUT_ID = "FRAGMENT_LAYOUT_ID";
    protected int fragmentLayoutId;*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static SimpleWpFragment getInstance(int wpFragmentLayoutId){
        final Bundle bundle = new Bundle();
        bundle.putInt(FRAGMENT_LAYOUT_ID, wpFragmentLayoutId);
        SimpleWpFragment wpFragment = new SimpleWpFragment();
        wpFragment.setArguments(bundle);
        return wpFragment;
    }
}
