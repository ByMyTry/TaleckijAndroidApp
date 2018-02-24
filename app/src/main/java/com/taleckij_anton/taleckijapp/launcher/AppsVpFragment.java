package com.taleckij_anton.taleckijapp.launcher;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.DesktopFragment;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppsFragment;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

/**
 * Created by Lenovo on 25.02.2018.
 */

public class AppsVpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.fragment_launcher_view_pager, container, false);

        final ViewPager viewPager = view.findViewById(R.id.apps_view_pager);
        viewPager.setAdapter(new SectionPagerAdapter(getChildFragmentManager()));

        return view;
    }

    class SectionPagerAdapter extends FragmentStatePagerAdapter{
        SectionPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment curFragment;
            switch (position){
                case 0:
                    curFragment = new DesktopFragment();

                    break;
                case 1:
                    curFragment = AppsFragment.getInstance(AppsFragment.APPS_GRID_LAYOUT);

                    YandexMetrica.reportEvent(MetricaAppEvents.AppsGridOpen);
                    break;
                case 2:
                    curFragment = AppsFragment.getInstance(AppsFragment.APPS_LINEAR_LAYOUT);

                    YandexMetrica.reportEvent(MetricaAppEvents.AppsLinearOpen);
                    break;
                /*case 3:
                    curFragment = new SettingsFragment();

                    YandexMetrica.reportEvent(MetricaAppEvents.AppsSettingsOpen);
                    break;*/
                default:
                    curFragment = new DesktopFragment();
            }
            return curFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int pos){
            CharSequence curTitle;
            switch (pos){
                case 0:
                    curTitle = getResources().getString(R.string.launcher_desk_nav_title);
                    break;
                case 1:
                    curTitle = getResources().getString(R.string.launcher_grid_nav_title);
                    break;
                case 2:
                    curTitle = getResources().getString(R.string.launcher_linear_nav_title);
                    break;
                default:
                    curTitle = getResources().getString(R.string.launcher_desk_nav_title);
            }
            return curTitle;
        }
    }
}
