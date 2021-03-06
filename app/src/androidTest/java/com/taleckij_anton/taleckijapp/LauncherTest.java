package com.taleckij_anton.taleckijapp;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Lenovo on 24.02.2018.
 */

@RunWith(AndroidJUnit4.class)
public class LauncherTest {
    @Rule
    public ActivityTestRule<LauncherActivity>
            mActivityRule = new ActivityTestRule<>(LauncherActivity.class);

    @Test
    public void fastPagesForward(){
        try {
            swipeLeftNTimes(R.id.launcher, 2);
        } catch (NoMatchingViewException e) {
            //first launch
            //TODO [переделать] мокнуть SharedPreference
            swipeLeftNTimes(R.id.wp_view_pager, 3);
            onView(withId(R.id.wp_finish_button)).perform(click());
            swipeLeftNTimes(R.id.launcher, 2);
        }
    }

    @Test
    public void openAboutMe(){
        try {
            clickPhotoInDrawer();
        } catch (NoMatchingViewException e) {
            //first launch
            //TODO [переделать] мокнуть SharedPreference
            swipeLeftNTimes(R.id.wp_view_pager, 3);
            onView(withId(R.id.wp_finish_button)).perform(click());
            clickPhotoInDrawer();
        }
    }

    private void clickPhotoInDrawer(){
        onView(withId(R.id.launcher))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_header_my_photo)).perform(click());
    }

    private void swipeLeftNTimes(int id, int n){
        for (int i = 0; i < n; i++) {
            onView(withId(id)).perform(swipeLeft());
        }
    }
}
