package com.taleckij_anton.taleckijapp.welcome_page;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.taleckij_anton.taleckijapp.R;

//import static com.taleckij_anton.taleckijapp.welcope_page.SimpleWpFragment.FRAGMENT_LAYOUT_ID;

/**
 * Created by Lenovo on 01.02.2018.
 */

public class SettingsWpFragment extends WpFragment {
    //private int mFragmentLayoutId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragmentLayoutId = getArguments().getInt(FRAGMENT_LAYOUT_ID);
        final View view = inflater.inflate(fragmentLayoutId, container, false);

        setCurrentRadioFromSp(view);

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
                final RadioButton lightThemeRadio = view.findViewById(R.id.radio_one);
                lightThemeRadio.setChecked(true);
            } else {
                final RadioButton darkThemeRadio = view.findViewById(R.id.radio_two);
                darkThemeRadio.setChecked(true);
            }
        } else {
            final String compactLayoutPrefKey = getResources().getString(R.string.compact_layout_preference_key);
            final boolean isCompactLayout = sharedPreferences.getBoolean(compactLayoutPrefKey, false);

            if(!isCompactLayout){
                final RadioButton normalLayoutRadio = view.findViewById(R.id.radio_one);
                normalLayoutRadio.setChecked(true);
            } else {
                final RadioButton compactLayoutRadio = view.findViewById(R.id.radio_two);
                compactLayoutRadio.setChecked(true);
            }
        }
    }

    @Override
    public void onStop() {
        Log.i("onStop", String.valueOf(isThemeLayout()));
        if(isThemeLayout()) {
            final RadioButton darkThemeRadio = getActivity().findViewById(R.id.radio_two);
            applyPrefToSharedPref(R.string.theme_preference_key, darkThemeRadio.isChecked());
            //final String themeDarkPrefKey = getResources().getString(R.string.theme_preference_key);
            //editor.putBoolean(themeDarkPrefKey, darkThemeRadio.isChecked())
            //        .apply();
        }else{
            final RadioButton compactLayoutRadio = getActivity().findViewById(R.id.radio_two);
            applyPrefToSharedPref(R.string.compact_layout_preference_key, compactLayoutRadio.isChecked());
        }
        super.onStop();
    }

    /*@Override
    public void onDestroyView() {
        Log.i("onDestroyView", String.valueOf(isThemeLayout()));
        if(isThemeLayout()) {
            final RadioButton darkThemeRadio = getActivity().findViewById(R.id.radio_two);
            applyPrefToSharedPref(R.string.theme_preference_key, darkThemeRadio.isChecked());
            //final String themeDarkPrefKey = getResources().getString(R.string.theme_preference_key);
            //editor.putBoolean(themeDarkPrefKey, darkThemeRadio.isChecked())
            //       .apply();
        }else{
            final RadioButton compactLayoutRadio = getActivity().findViewById(R.id.radio_two);
            applyPrefToSharedPref(R.string.compact_layout_preference_key, compactLayoutRadio.isChecked());
        }
        super.onDestroyView();
    }*/

    private void applyPrefToSharedPref(int prefKeyResId, Boolean value){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor spEditor = sharedPreferences.edit();
        final String prefKey = getResources().getString(prefKeyResId);
        spEditor.putBoolean(prefKey, value)
                .apply();
    }
}
