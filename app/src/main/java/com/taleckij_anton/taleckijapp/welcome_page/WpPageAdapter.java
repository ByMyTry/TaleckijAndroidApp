package com.taleckij_anton.taleckijapp.welcome_page;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.taleckij_anton.taleckijapp.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Lenovo on 28.02.2018.
 */

public class WpPageAdapter extends FragmentStatePagerAdapter {
    private final static String SIMPLE_WP_FRAGMENT = "SIMPLE_WP_FRAGMENT";
    private final static String SETTINGS_WP_FRAGMENT = "SETTINGS_WP_FRAGMENT";

    private final ArrayList<WpFragmentInfo> mWpFragmentsInfo = new ArrayList<>(Arrays.asList(
            new WpFragmentInfo(R.layout.fragment_wp_hello, SIMPLE_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_about_app, SIMPLE_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_theme_choice, SETTINGS_WP_FRAGMENT),
            new WpFragmentInfo(R.layout.fragment_wp_density_choice, SETTINGS_WP_FRAGMENT)
    ));

    private class WpFragmentInfo {
        final Integer wpFragmentLayoutId;
        final String wpFragmentType;

        WpFragmentInfo(Integer wpFragmentLayoutId, String wpFragmentType){
            this.wpFragmentLayoutId = wpFragmentLayoutId;
            this.wpFragmentType = wpFragmentType;
        }
    }

    public WpPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return wpFragmentFromWpInfo(mWpFragmentsInfo.get(position));
    }

    @Override
    public int getCount() {
        return mWpFragmentsInfo.size();
    }

    private WpFragment wpFragmentFromWpInfo(WpFragmentInfo wpFragmentInfo) {
        final WpFragment wpFragment;
        if (SIMPLE_WP_FRAGMENT.equals(wpFragmentInfo.wpFragmentType)) {
            wpFragment = SimpleWpFragment.getInstance(wpFragmentInfo.wpFragmentLayoutId);
        } else {
            wpFragment = SettingsWpFragment.getInstance(wpFragmentInfo.wpFragmentLayoutId);
        }
        return wpFragment;
    }
}
