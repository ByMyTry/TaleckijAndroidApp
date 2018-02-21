package com.taleckij_anton.taleckijapp.launcher.desktop_fragment;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDb;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbHelper;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.recycler.DesktopAppsAdapter;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppViewModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.OnAppsViewGestureActioner;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.recycler.AppsAdapter;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lenovo on 19.02.2018.
 */

public class DesktopFragment extends Fragment {
    //private ImageView mGarbageSpace;
    private RecyclerView mRecyclerView;
    private @Nullable UserHandle mUser;
    private AppsDbHelper mAppsDbHelper;

    public static final int DESKTOP_APPS_TOTAL_COUNT = 6;
    public static final int DESKTOP_APPS_ROW_COUNT = 3;
    //public static final int DESKTOP_APPS_ROW_COUNT_PORT = 3;
    //public static final int DESKTOP_APPS_ROW_COUNT_LAND = 4;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View desktopView =
                inflater.inflate(R.layout.fragment_launcher_desktop, container, false);
        //mGarbageSpace = desktopView.findViewById(R.id.desktop_garbage_space);
        mRecyclerView = desktopView.findViewById(R.id.desktop_apps_recycler);

        mAppsDbHelper = new AppsDbHelper(desktopView.getContext());

        final List<UserHandle> userHandles = getUserHandles(desktopView.getContext());
        if(userHandles != null) {
            mUser = userHandles.get(0);
        }
        final List<LauncherActivityInfo> applicationInfos =
                getApplicationInfos(desktopView.getContext(), mUser);
        final List<AppInfoModel> deskAppModels = synchronizeWithDb(applicationInfos);

        //~создать RecyclerView из актуального списка приложений рабочего стола
        /*for(AppInfoModel appInfoModel : deskAppModels){
            if(appInfoModel.getDesktopPosition() != null){
                Log.d("DESKTOP", appInfoModel.getFullName());
            }
        }*/
        createRecyclerView(deskAppModels, DESKTOP_APPS_ROW_COUNT);

        return desktopView;
    }

    /*private List<DesktopAppViewModel> getCurrentDeskApps(){

    }*/

    private List<AppInfoModel> synchronizeWithDb(List<LauncherActivityInfo>
                                                         applicationInfos) {
        LinkedList<AppInfoModel> currentAppsModels = new LinkedList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Cursor desktopPosCursor = null;
        try {
            db = mAppsDbHelper.getReadableDatabase();
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
                    db = mAppsDbHelper.getWritableDatabase();
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

    private @Nullable List<UserHandle> getUserHandles(Context context){
        final UserManager userManager = (UserManager)
                context.getSystemService(Context.USER_SERVICE);
        return userManager != null ? userManager.getUserProfiles() : null ;
    }

    private @Nullable List<LauncherActivityInfo> getApplicationInfos(Context context,
                                                                     UserHandle userHandle){
        final LauncherApps launcherApps =
                (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        List<LauncherActivityInfo> applicationInfos = null;
        if(launcherApps != null)
            applicationInfos = launcherApps.getActivityList(null, userHandle);
        return applicationInfos;
    }

    private void createRecyclerView(List<AppInfoModel> appModels,
                                    int spanCount){
        mRecyclerView.setLayoutManager(
                new GridLayoutManager(mRecyclerView.getContext(), spanCount));
        final OnDeskAppsViewGestureActioner onDeskAppsViewGestureActioner =
                new OnDeskAppsViewGestureActioner() {
                    @Override
                    public void launchApp(Context context, AppInfoModel incrementedAppModel){
                        final LauncherApps launcherApps =
                                (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                        incrementLaunchCountDb(incrementedAppModel);
                        if(launcherApps != null) {
                            launcherApps.startMainActivity(
                                    incrementedAppModel.getComponentName(),
                                    mUser, null, null);
                        }

                        YandexMetrica.reportEvent(MetricaAppEvents.AppOpen,
                                String.format("{\"app_name\":%s}", incrementedAppModel.getLabel()));
                    }

                    @Override
                    public void startDrag(View v, AppInfoModel appModel) {
                        //mGarbageSpace.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void stopDrag(View v, AppInfoModel appModelWithNewDescPos) {
                        //mGarbageSpace.setVisibility(View.INVISIBLE);
                        updateDeskPosDb(appModelWithNewDescPos);
                    }
                };
        DesktopAppsAdapter adapter =
                new DesktopAppsAdapter(appModels, onDeskAppsViewGestureActioner);
        mRecyclerView.setAdapter(adapter);
    }

    private void incrementLaunchCountDb(AppInfoModel incrementedAppModel){
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(AppsDb.CountTableColumns.FIELD_APP_LAUNCH_COUNT,
                incrementedAppModel.getLaunchCount());
        try {
            db = mAppsDbHelper.getWritableDatabase();
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

    private void updateDeskPosDb(AppInfoModel appModelWithNewDescPos){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int appId = -1;
        try {
            db = mAppsDbHelper.getReadableDatabase();
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
                db = mAppsDbHelper.getWritableDatabase();
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
}
