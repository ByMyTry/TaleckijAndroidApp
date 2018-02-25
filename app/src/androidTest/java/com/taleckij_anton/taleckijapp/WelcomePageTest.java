package com.taleckij_anton.taleckijapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

/**
 * Created by Lenovo on 24.02.2018.
 */

@RunWith(AndroidJUnit4.class)
public class WelcomePageTest {

    @Rule
    public ActivityTestRule<WelcomePageActivity>
            mActivityRule = new ActivityTestRule<>(WelcomePageActivity.class);

    /*@Test
    public void defaultTheme() {
        clickNTimes(R.id.button_next, 2);
        onView(withId(R.id.radio_one)).check(matches(isChecked()));
        onView(withId(R.id.radio_two)).check(matches(not(isChecked())));
        //onView(withId(R.id.list)).perform(RecyclerViewActions.scrollTo(withChild(withText(mPackageName))));
        //onView(allOf(withId(R.id.content), withText(mPackageName))).check(matches(isDisplayed()));
    }

    @Test
    public void defaultLayoutType() {
        clickNTimes(R.id.button_next, 3);
        onView(withId(R.id.radio_one)).check(matches(isChecked()));
        onView(withId(R.id.radio_two)).check(matches(not(isChecked())));
    }*/

    @Test
    public void changeThemeToLight() {
        clickNTimes(R.id.button_next, 2);
        onView(withId(R.id.radio_one)).perform(click());
        onView(withId(R.id.radio_two)).check(matches(not(isChecked())));
        onView(withId(R.id.radio_one)).check(matches(isChecked()));
    }

    @Test
    public void changeThemeToDark() {
        clickNTimes(R.id.button_next, 2);
        onView(withId(R.id.radio_two)).perform(click());
        onView(withId(R.id.radio_one)).check(matches(not(isChecked())));
        onView(withId(R.id.radio_two)).check(matches(isChecked()));
        onView(withId(R.id.radio_one)).perform(click());
    }

    @Test
    public void changeLayoutTypeToNormal() {
        clickNTimes(R.id.button_next, 3);
        onView(withId(R.id.radio_one)).perform(click());
        onView(withId(R.id.radio_two)).check(matches(not(isChecked())));
        onView(withId(R.id.radio_one)).check(matches(isChecked()));
    }

    @Test
    public void changeLayoutTypeToCompact() {
        clickNTimes(R.id.button_next, 3);
        onView(withId(R.id.radio_two)).perform(click());
        onView(withId(R.id.radio_one)).check(matches(not(isChecked())));
        onView(withId(R.id.radio_two)).check(matches(isChecked()));
        onView(withId(R.id.radio_one)).perform(click());
    }

    private void clickNTimes(int id, int n){
        for (int i = 0; i < n; i++) {
            onView(withId(id)).perform(click());
        }
    }
}
