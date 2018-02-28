package com.taleckij_anton.taleckijapp.launcher.apps_db;

import android.content.ContentValues;
import android.content.pm.LauncherActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.DesktopFragment;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lenovo on 24.02.2018.
 */

public class AppsDbSynchronizer {
    private static final Object mLock = new Object();
    private static volatile AppsDbSynchronizer sInstance;

    private AppsDbSynchronizer(){}

    public static AppsDbSynchronizer getInstance(){
        if(null == sInstance){
            synchronized (mLock){
                if(null == sInstance){
                    sInstance = new AppsDbSynchronizer();
                    return sInstance;
                }
            }
        }

        return sInstance;
    }

    public void addAppsToDb(AppsDbHelper appsDbHelper, List<AppInfoModel> addedAppModels){
        SQLiteDatabase db = null;
        try {
            db = appsDbHelper.getWritableDatabase();
            for(AppInfoModel appModel: addedAppModels) {
                ContentValues values = new ContentValues();
                values.put(AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT,
                        appModel.getLaunchCount());
                //values.put(AppsDb.Columns.FIELD_APP_PACKAGE_NAME,
                //        appModel.getPackageName());
                values.put(AppsDb.CountTableColumns.FILED_APP_FULL_NAME,
                        appModel.getFullName());
                db.insert(AppsDb.APPS_LUNCH_COUNT_TABLE, null, values);
            }
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    public void removeAppsFromDb(AppsDbHelper appsDbHelper, List<AppInfoModel> removedAppModels){
        SQLiteDatabase db = null;
        try {
            db = appsDbHelper.getWritableDatabase();
            for(AppInfoModel appModel: removedAppModels) {
                db.delete(
                        AppsDb.APPS_LUNCH_COUNT_TABLE,
                        AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                        new String[]{appModel.getFullName()}
                );
            }
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    public void clearData(AppsDbHelper appsDbHelper) {
        try {
            SQLiteDatabase db = appsDbHelper.getWritableDatabase();
            db.delete(AppsDb.APPS_LUNCH_COUNT_TABLE, null, null);
            db.close();
        } catch (SQLiteException e) {}
    }

    public List<AppInfoModel> synchronizeWithDb( AppsDbHelper appsDbHelper,
                                                 List<LauncherActivityInfo> applicationInfos) {
        LinkedList<AppInfoModel> currentAppsModels = new LinkedList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Cursor desktopPosCursor = null;
        try {
            db = appsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID,
                            AppsDb.CountTableColumns.FILED_APP_FULL_NAME,
                            AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT },
                    null, null, null, null, null);
            List<String> appsFullNameDb = new LinkedList<>();
            List<Integer> appsLaunchCountsDb = new LinkedList<>();
            List<Integer> appsDesktopPosDb = new LinkedList<>();
            while(cursor.moveToNext()){
                int id = cursor.getInt(0);
                appsFullNameDb.add(cursor.getString(1));
                appsLaunchCountsDb.add(cursor.getInt(2));
                desktopPosCursor = db.query(AppsDb.DESKTOP_APPS_TABLE,
                        new String []{AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION},
                        AppsDb.DesktopTableColumns.FIELD_APP_ID + " LIKE ?",
                        new String[]{String.valueOf(id)},
                        null, null, null);
                if(desktopPosCursor.moveToNext()){
                    appsDesktopPosDb.add(desktopPosCursor.getInt(0));
                } else {
                    appsDesktopPosDb.add(null);
                }
            }
            appsFullNameDb = new ArrayList<>(appsFullNameDb);
            appsLaunchCountsDb = new ArrayList<>(appsLaunchCountsDb);
            appsDesktopPosDb = new ArrayList<>(appsDesktopPosDb);
            for (LauncherActivityInfo appInfo : applicationInfos) {
                AppInfoModel appModel;
                if(appsFullNameDb.contains(appInfo.getName())){
                    int index = appsFullNameDb.indexOf(appInfo.getName());
                    appModel = new AppInfoModel(
                            appInfo,
                            appsLaunchCountsDb.get(index),
                            new AppViewModel(null, null),
                            appsDesktopPosDb.get(index)
                    );
                } else {
                    db.close();
                    db = appsDbHelper.getWritableDatabase();
                    appModel = new AppInfoModel(
                            appInfo,
                            0,
                            new AppViewModel(null, null),
                            null
                    );
                    ContentValues values = new ContentValues();
                    values.put(AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT,
                            appModel.getLaunchCount());
                    //values.put(AppsDb.Columns.FIELD_APP_PACKAGE_NAME,
                    //        appModel.getPackageName());
                    values.put(AppsDb.CountTableColumns.FILED_APP_FULL_NAME,
                            appModel.getFullName());
                    db.insert(AppsDb.APPS_LUNCH_COUNT_TABLE, null, values);
                }
                currentAppsModels.add(appModel);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if(desktopPosCursor != null){desktopPosCursor.close();}
            if(cursor != null)cursor.close();
            if(db != null)db.close();
        }
        return new ArrayList<>(currentAppsModels);
    }

    /*private boolean isDesktopApp(String appFullName){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean res = false;
        try {
            db = mAppsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID},
                    AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{appFullName},
                    null, null, null
            );
            if(cursor.moveToNext()){
                res = true;
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
        return res;
    }*/

    public void updateDeskPosDb(AppsDbHelper appsDbHelper, AppInfoModel appModelWithNewDescPos){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int appId = -1;
        try {
            db = appsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID},
                    AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{appModelWithNewDescPos.getFullName()},
                    null, null, null
            );
            if(cursor.moveToNext()){
                appId = cursor.getInt(0);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
        if(appId != -1) {
            ContentValues values = new ContentValues();
            values.put(AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION,
                    appModelWithNewDescPos.getDesktopPosition());
            db = null;
            try {
                db = appsDbHelper.getWritableDatabase();
                db.update(
                        AppsDb.DESKTOP_APPS_TABLE,
                        values,
                        AppsDb.DesktopTableColumns.FIELD_APP_ID + " LIKE ?",
                        new String[]{String.valueOf(appId)}
                );
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (db != null) db.close();
            }
        }
    }

    public void incrementLaunchCountDb(AppsDbHelper appsDbHelper,
                                        AppInfoModel incrementedAppModel){
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT,
                incrementedAppModel.getLaunchCount());
        try {
            db = appsDbHelper.getWritableDatabase();
            db.update(
                    AppsDb.APPS_LUNCH_COUNT_TABLE,
                    values,
                    AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{incrementedAppModel.getFullName()}
            );
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    public int getFirstAvailableDeskPos(AppsDbHelper appsDbHelper){
        List<Integer> notAvailableDeskPositions = new LinkedList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = appsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.DESKTOP_APPS_TABLE,
                    new String[]{AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION},
                    null, null, null, null, null);
            while(cursor.moveToNext()){
                notAvailableDeskPositions.add(cursor.getInt(0));
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null)cursor.close();
            if(db != null)db.close();
        }
        for (int i = 0; i < DesktopFragment.DESKTOP_APPS_TOTAL_COUNT; i++) {
            if(!notAvailableDeskPositions.contains(i)){
                return i;
            }
        }
        return -1;
    }

    public void addToDesktopDb(AppsDbHelper appsDbHelper, int desktopPosition, String appFullName){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int appId = -1;
        try {
            db = appsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID},
                    AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{appFullName},
                    null, null, null
            );
            if(cursor.moveToNext()){
                appId = cursor.getInt(0);
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
        if(appId != -1) {
            ContentValues values = new ContentValues();
            values.put(AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION, desktopPosition);
            values.put(AppsDb.DesktopTableColumns.FIELD_APP_ID, appId);
            db = null;
            try {
                db = appsDbHelper.getWritableDatabase();
                db.insert(AppsDb.DESKTOP_APPS_TABLE,null, values);
            } catch (SQLiteException e) {
            } finally {
                if(db != null)db.close();
            }
        }
    }

    public void removeFromDesktopDb(AppsDbHelper appsDbHelper, int desktopPosition){
        SQLiteDatabase db = null;
        try {
            db = appsDbHelper.getWritableDatabase();
            db.delete(
                    AppsDb.DESKTOP_APPS_TABLE,
                    AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION + " LIKE ?",
                    new String[]{String.valueOf(desktopPosition)}
            );
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    public int getCurrentDeskPos(AppsDbHelper appsDbHelper, String appFullName){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int appId = -1;
        try {
            db = appsDbHelper.getReadableDatabase();
            cursor = db.query(AppsDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsDb.CountTableColumns._ID},
                    AppsDb.CountTableColumns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{appFullName},
                    null, null, null
            );
            if(cursor.moveToNext()){
                appId = cursor.getInt(0);
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null) cursor.close();
            if(db != null) db.close();
        }
        int currentDeskPos = -1;
        if(appId != -1) {
            try {
                db = appsDbHelper.getReadableDatabase();
                cursor = db.query(
                        AppsDb.DESKTOP_APPS_TABLE,
                        new String[]{AppsDb.DesktopTableColumns.FIELD_APP_DESKTOP_POSITION},
                        AppsDb.DesktopTableColumns.FIELD_APP_ID + " LIKE ?",
                        new String[]{String.valueOf(appId)},
                        null, null, null, null
                );
                while(cursor.moveToNext()){
                    currentDeskPos = cursor.getInt(0);
                }
            } catch (SQLiteException e) {
            } finally {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }
        }
        return currentDeskPos;
    }
}
