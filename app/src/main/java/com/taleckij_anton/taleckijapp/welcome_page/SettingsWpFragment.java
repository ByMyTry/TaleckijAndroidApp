package com.taleckij_anton.taleckijapp.welcome_page;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.WrapperListAdapter;

import com.taleckij_anton.taleckijapp.LauncherActivity;
import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.WelcomePageActivity;

import java.sql.Array;
import java.util.Arrays;

//import static com.taleckij_anton.taleckijapp.welcope_page.SimpleWpFragment.FRAGMENT_LAYOUT_ID;

/**
 * Created by Lenovo on 01.02.2018.
 */

public class SettingsWpFragment extends WpFragment {
//    private int mFragmentLayoutId;
    private RadioButton mFirstRadio;
    private View mFirstLayoutRadio;
    private RadioButton mSecondRadio;
    private View mSecondLayoutRadio;

    private final View.OnClickListener mOnRadioClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int clickedViewId = v.getId();
            if(clickedViewId == mFirstRadio.getId()
                    || clickedViewId == mFirstLayoutRadio.getId()){
                setCheckedIfNecessary(mFirstRadio, mSecondRadio);
            } else if(clickedViewId == mSecondRadio.getId()
                    || clickedViewId == mSecondLayoutRadio.getId()){
                setCheckedIfNecessary(mSecondRadio, mFirstRadio);
            }
        }

        private void setCheckedIfNecessary(RadioButton checked, RadioButton unchecked){
            if(checked.isChecked() == false){
                checked.setChecked(true);
                unchecked.setChecked(false);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragmentLayoutId = getArguments().getInt(FRAGMENT_LAYOUT_ID);
        final View view = inflater.inflate(fragmentLayoutId, container, false);

        setCurrentRadioFromSp(view);
        final View finishButton = view.findViewById(R.id.wp_finish_button);
        if(finishButton != null) {
            View.OnClickListener onFinishClick = ((WelcomePageActivity)getActivity()).onFinishButtonClick;
            finishButton.setOnClickListener(onFinishClick);
        }

        if(isThemeLayout()) {
            mFirstRadio = view.findViewById(R.id.radio_theme_one);
            mFirstLayoutRadio = view.findViewById(R.id.layout_radio_theme_one);
            mSecondRadio = view.findViewById(R.id.radio_theme_two);
            mSecondLayoutRadio = view.findViewById(R.id.layout_radio_theme_two);
        } else {
            mFirstRadio = view.findViewById(R.id.radio_layout_one);
            mFirstLayoutRadio = view.findViewById(R.id.layout_radio_layout_one);
            mSecondRadio = view.findViewById(R.id.radio_layout_two);
            mSecondLayoutRadio = view.findViewById(R.id.layout_radio_layout_two);
        }

        for(View clicked :
                Arrays.asList(mFirstRadio, mFirstLayoutRadio, mSecondRadio, mSecondLayoutRadio)){
            clicked.setOnClickListener(mOnRadioClick);
        }

        return view;
    }

    private boolean isThemeLayout(){
        return R.layout.fragment_wp_theme_choice == fragmentLayoutId;
    }

    private void setCurrentRadioFromSp(View view){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(isThemeLayout()){
            final String themeDarkPrefKey = getResources().getString(R.string.theme_preference_key);
            final boolean isDarkTheme = sharedPreferences.getBoolean(themeDarkPrefKey, false);

            if(!isDarkTheme){
                final RadioButton lightThemeRadio = view.findViewById(R.id.radio_theme_one);
                lightThemeRadio.setChecked(true);
            } else {
                final RadioButton darkThemeRadio = view.findViewById(R.id.radio_theme_two);
                darkThemeRadio.setChecked(true);
            }
        } else {
            final String compactLayoutPrefKey = getResources().getString(R.string.compact_layout_preference_key);
            final boolean isCompactLayout = sharedPreferences.getBoolean(compactLayoutPrefKey, false);

            if(!isCompactLayout){
                final RadioButton normalLayoutRadio = view.findViewById(R.id.radio_layout_one);
                normalLayoutRadio.setChecked(true);
            } else {
                final RadioButton compactLayoutRadio = view.findViewById(R.id.radio_layout_two);
                compactLayoutRadio.setChecked(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isThemeLayout()) {
            final RadioButton darkThemeRadio = getView().findViewById(R.id.radio_theme_two);
            applyPrefToSharedPref(R.string.theme_preference_key, darkThemeRadio.isChecked());
            //final String themeDarkPrefKey = getResources().getString(R.string.theme_preference_key);
            //editor.putBoolean(themeDarkPrefKey, darkThemeRadio.isChecked())
            //        .apply();
        } else {
            final RadioButton compactLayoutRadio = getView().findViewById(R.id.radio_layout_two);
            applyPrefToSharedPref(R.string.compact_layout_preference_key, compactLayoutRadio.isChecked());
        }
    }

    private void applyPrefToSharedPref(int prefKeyResId, Boolean value){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor spEditor = sharedPreferences.edit();
        final String prefKey = getResources().getString(prefKeyResId);
        spEditor.putBoolean(prefKey, value)
                .commit();
    }

    public static SettingsWpFragment getInstance(int wpFragmentLayoutId){
        final Bundle bundle = new Bundle();
        bundle.putInt(FRAGMENT_LAYOUT_ID, wpFragmentLayoutId);
        SettingsWpFragment wpFragment = new SettingsWpFragment();
        wpFragment.setArguments(bundle);
        return wpFragment;
    }
}
