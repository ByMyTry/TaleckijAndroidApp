package com.taleckij_anton.taleckijapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.net.ConnectivityManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Lenovo on 24.02.2018.
 */

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    @Test
    public void checkConnection(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        ConnectivityManager cm =
                (ConnectivityManager) appContext.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm != null && cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        assertEquals(true, isNetworkConnected);
    }

    @Test
    public void checkApps(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        final LauncherApps launcherApps = (LauncherApps)
                appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        assertThat(launcherApps, notNullValue());
    }

    @Test
    public void checkUser(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        final UserManager userManager = (UserManager)
                appContext.getSystemService(Context.USER_SERVICE);
        final List<UserHandle> userHandles =
                userManager != null ? userManager.getUserProfiles() : null;
        boolean userExists = userHandles != null && userHandles.size() > 0;

        assertEquals(true, userExists);
    }

    @Test
    public void checkSharedPreference(){
        Context appContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);

        assertThat(sp, notNullValue());

    }
}
