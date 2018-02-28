package com.taleckij_anton.taleckijapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.taleckij_anton.taleckijapp.background_images.ImageSaver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Lenovo on 23.02.2018.
 */

@RunWith(RobolectricTestRunner.class)
public class ImageSaverTest {

    private Context mContext;
    private Bitmap mTestBitmap;
    private final ImageSaver mImageSaver = ImageSaver.getInstance();
    private final static String mTestImgName = "imgName";

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mTestBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.header_nav_view_image);
    }

    @Test
    public void imageProcess() {
        mImageSaver.saveImage(mContext, mTestBitmap, mTestImgName);
        assertNotEquals(null, mImageSaver.loadImage(mContext, mTestImgName));
    }

    @Test
    public void clearProcess(){
        mImageSaver.saveImage(mContext, mTestBitmap, mTestImgName);
        mImageSaver.clear(mContext);
        assertEquals(null, mImageSaver.loadImage(mContext, mTestImgName));
    }
}


//private Context mContext;
//private final WelcomePageActivity mWelcomePageActivity
//        = Robolectric.setupActivity(WelcomePageActivity.class);

    /*@Before
    public void setUp(){
        mContext = RuntimeEnvironment.application;
    }*/

    /*@Test
    public void defaultWpSettingsUi() {
        Button button = mWelcomePageActivity.findViewById(R.id.button_next);
        performClickNTimes(button, 2);
        assertRadioUiState(true, false);
        button.performClick();
        assertRadioUiState(true, false);
    }*/

    /*@Test
    public void defaultWpSettingsSp(){
        Button button = mWelcomePageActivity.findViewById(R.id.button_next);
        performClickNTimes(button, 2);
        assertLightThemeInSp(true);
        button.performClick();
        assertNormalLayoutInSp(true);
    }*/

    /*@Test
    public void changeWpSettingsUi() {
        clickSecondRadioAfterClickNext(2);
        assertRadioUiState(false, true);
        clickSecondRadioAfterClickNext(1);
        assertRadioUiState(false, true);
    }*/

   /* @Test
    public void changeWpSettingsSp() {
        clickSecondRadioAfterClickNext(2);
        clickSecondRadioAfterClickNext(1);
        assertLightThemeInSp(false);
        performClickNTimes(null, 1);
        assertNormalLayoutInSp(false);
    }

    private void assertLightThemeInSp(boolean isLightThemeExpected) {
        SharedPreferences sp =PreferenceManager.getDefaultSharedPreferences(mWelcomePageActivity);
        String isDarkThemeKey =
                mWelcomePageActivity.getResources().getString(R.string.theme_preference_key);
        boolean isDarkThemeInSp = sp.getBoolean(isDarkThemeKey, false);
        boolean isLightThemeInSp = !isDarkThemeInSp;
        assertEquals(isLightThemeExpected, isLightThemeInSp);
    }

    private void assertNormalLayoutInSp(boolean isNormalLayoutExpected) {
        SharedPreferences sp =PreferenceManager.getDefaultSharedPreferences(mWelcomePageActivity);
        String isCompactLayoutKey = mWelcomePageActivity
                .getResources().getString(R.string.compact_layout_preference_key);
        boolean isCompactLayout = sp.getBoolean(isCompactLayoutKey, false);
        boolean isNormalLayout = !isCompactLayout;
        assertEquals(isNormalLayoutExpected, isNormalLayout);
    }

    private void assertRadioUiState(boolean firstRadioUiState, boolean secondRadioUiState) {
        assertEquals(firstRadioUiState,
                ((RadioButton)mWelcomePageActivity.findViewById(R.id.radio_one)).isChecked());
        assertEquals(secondRadioUiState,
                ((RadioButton)mWelcomePageActivity.findViewById(R.id.radio_two)).isChecked());
    }

    private void clickSecondRadioAfterClickNext(int clickNextCount) {
        Button button = mWelcomePageActivity.findViewById(R.id.button_next);
        performClickNTimes(button, clickNextCount);
        RadioButton radioTwo = mWelcomePageActivity.findViewById(R.id.radio_two);
        radioTwo.performClick();
    }

    private void performClickNTimes(@Nullable View buttonNext, int n){
        if(buttonNext == null){
            buttonNext = mWelcomePageActivity.findViewById(R.id.button_next);
        }
        for (int i = 0; i < n; i++) {
            buttonNext.performClick();
        }
    }*/