package com.taleckij_anton.taleckijapp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDb;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbHelper;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbSynchronizer;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Lenovo on 24.02.2018.
 */

@RunWith(RobolectricTestRunner.class)
public class AppsDbSynchronizerTest {
    private Context mContext;
    private AppsDbHelper mAppsDbHelper;
    private final AppsDbSynchronizer mAppsDbSynchronizer = AppsDbSynchronizer.getInstance();

    @Mock
    private AppInfoModel mAppInfoModel;

    private final String mAppFullName = "app_full_name_1";
    private final int mAppLaunchCount = 0;

    @Before
    public void setUp() {
        mContext = RuntimeEnvironment.application;
        mAppsDbHelper = new AppsDbHelper(mContext);
    }

    @Test
    public void addAppsToDb(){
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mAppsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns.FILED_APP_FULL_NAME,
                            AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT},
                    null, null, null, null, null
                    );
            assertEquals(1, cursor.getCount());
            while(cursor.moveToNext()){
                assertEquals(mAppFullName, cursor.getString(0));
                assertEquals(mAppLaunchCount, cursor.getInt(1));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
            if(db != null){db.close();}
        }
    }

    @Test
    public void removeAppsFromDb(){
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        mAppsDbSynchronizer.removeAppsFromDb(mAppsDbHelper, appList);

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mAppsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns.FILED_APP_FULL_NAME,
                            AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT},
                    null, null, null, null, null
            );
            assertEquals(0, cursor.getCount());
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
            if(db != null){db.close();}
        }
    }

    @Test
    public void defaultFirstAvailableDeskPos() {
        int pos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        assertEquals(pos, 0);
    }

    @Test
    public void addToDesktopDb(){
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        int defPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, defPos, mAppFullName);

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mAppsDbHelper.getReadableDatabase();
            int appId = -1;
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID},
                    null, null, null, null, null
            );
            assertEquals(1, cursor.getCount());
            while(cursor.moveToNext()){
                appId = cursor.getInt(0);
            }
            assertNotEquals(-1, appId);
            cursor = db.query(AppsDb.DESKTOP_APPS_TABLE,
                    new String[]{AppsDb.DesktopTableColumns.FIELD_APP_ID},
                    null, null, null, null, null
            );
            assertEquals(1, cursor.getCount());
            while(cursor.moveToNext()){
                assertEquals(appId, cursor.getInt(0));
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
            if(db != null){db.close();}
        }
    }

    @Test
    public void removeFromDeskDb(){
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        int defPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, defPos, mAppFullName);

        mAppsDbSynchronizer.removeFromDesktopDb(mAppsDbHelper, defPos);

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mAppsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.DESKTOP_APPS_TABLE,
                    new String[]{AppsDb.DesktopTableColumns.FIELD_APP_ID},
                    null, null, null, null, null
            );
            assertEquals(0, cursor.getCount());
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            if(cursor != null){cursor.close();}
            if(db != null){db.close();}
        }
    }

    @Test
    public void firstAvailableDeskPosAfterAddOne() {
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        int defPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, defPos, mAppFullName);

        int pos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        assertEquals(pos, 1);
    }

    @Test
    public void firstAvailableDeskPosAfterAddOneAndRemove() {
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        int defPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, defPos, mAppFullName);

        mAppsDbSynchronizer.removeFromDesktopDb(mAppsDbHelper, defPos);
        int pos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        assertEquals(pos, 0);
    }

    @Test
    public void updateDeskPosDb(){
        MockitoAnnotations.initMocks(this);

        when(mAppInfoModel.getFullName()).thenReturn(mAppFullName);
        when(mAppInfoModel.getLaunchCount()).thenReturn(mAppLaunchCount);
        List<AppInfoModel> appList = new ArrayList<>();
        appList.add(mAppInfoModel);

        mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, appList);
        int defPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, defPos, mAppFullName);

        when(mAppInfoModel.getDesktopPosition()).thenReturn(defPos + 1);
        mAppsDbSynchronizer.updateDeskPosDb(mAppsDbHelper, mAppInfoModel);

        int pos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
        assertEquals(defPos, pos);
    }
}
