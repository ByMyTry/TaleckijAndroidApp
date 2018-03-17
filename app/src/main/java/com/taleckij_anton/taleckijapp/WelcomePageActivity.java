package com.taleckij_anton.taleckijapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.taleckij_anton.taleckijapp.welcome_page.WpPageAdapter;
import com.yandex.metrica.YandexMetrica;

public class WelcomePageActivity extends BaseActivity {
    public final View.OnClickListener onFinishButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openLauncher(v.getContext());
        }
    };

    private ViewPager mWpViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

//        final ViewPager wpViewPager = findViewById(R.id.wp_view_pager);
        mWpViewPager = findViewById(R.id.wp_view_pager);
        mWpViewPager.setAdapter(new WpPageAdapter(getSupportFragmentManager()));

        final TabLayout wpPageIndicator = findViewById(R.id.wp_pager_indicator);
        wpPageIndicator.setupWithViewPager(mWpViewPager, true);

        YandexMetrica.reportEvent(MetricaAppEvents.WelcomePageOpen);
    }

    @Nullable
    @Override
    protected View getBackView() {
        return findViewById(R.id.wp_fragment_place);
    }

    @Override
    protected long getCurrentViewPageIndex() {
        return mWpViewPager.getCurrentItem();
    }

    @Override
    protected void openViewPage(long index) {
        mWpViewPager.setCurrentItem((int)index);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setCurrentViewItemIfNecessary();
    }

    private void setCurrentViewItemIfNecessary(){
        final long savedItemPos = getIntent().getLongExtra(CURRENT_VIEW_PAGE_INDEX, -1);
        if(savedItemPos != -1){
            openViewPage(savedItemPos);
        }
    }

    private void openLauncher(final Context context){
        final Intent intent = new Intent();
        intent.setClass(context, LauncherActivity.class);
        intent.putExtra(NavigatorActivity.LaunchWpIntentKey, false);
        setLaunchWpFalse();
        startActivity(intent);
    }

    private void setLaunchWpFalse(){
        final String launchWpPrefKey = getResources().getString(R.string.launch_wp_pref_key);
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences
                .edit()
                .putBoolean(launchWpPrefKey, false)
                .apply();
    }
}
