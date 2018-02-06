package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.db.AppsLaunchCountDb;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.db.AppsLaunchDbHelper;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.recycler.AppsAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private AppsChangeReceiver mAppsChangeReceiver;
    private @Nullable UserHandle mUser;
    private AppsLaunchDbHelper mAppsLaunchDbHelper;

    private final OnAppsChangeListener
            onAppsChangeListener = new OnAppsChangeListener() {
        @Override
        public void onAppInstalled(int addedAppsUid) {
            final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(mUser);
            final List<LaunchAppInfoModel> addedAppModels =
                    getAddedAppsModelsByUid(applicationInfos, addedAppsUid);
            final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
            adapter.updateAfterAdd(addedAppModels, addedAppsUid);
            addAppsToDb(addedAppModels);
        }

        @Override
        public void onAppRemoved(int removedAppsUid) {
            //final List<UserHandle> userHandles = getUserHandles();
            //final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(mUser);
            final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
            List<LaunchAppInfoModel> removedAppsModels = adapter.updateAfterRemove(removedAppsUid);
            removeAppsFromDb(removedAppsModels);
        }
    };

    public static AppsFragment getInstance(){
        return new AppsFragment();
    }

    private List<LaunchAppInfoModel> getAddedAppsModelsByUid(List<LauncherActivityInfo> appsData,
                                                             int uid){
        LinkedList<LaunchAppInfoModel> addedAppsModels = new LinkedList<>();
        for (LauncherActivityInfo appData: appsData){
            LaunchAppInfoModel appModel = new LaunchAppInfoModel(appData, 0);
            if(appModel.getUid() == uid) {
                addedAppsModels.add(appModel);
            }
        }
        return new ArrayList<>(addedAppsModels);
    }

    private void addAppsToDb(List<LaunchAppInfoModel> addedAppModels){
        SQLiteDatabase db = null;
        try {
            db = mAppsLaunchDbHelper.getWritableDatabase();
            for(LaunchAppInfoModel appModel: addedAppModels) {
                ContentValues values = new ContentValues();
                values.put(AppsLaunchCountDb.Columns.FIELD_APP_LAUNCH_COUNT,
                        appModel.getLaunchCount());
                //values.put(AppsLaunchCountDb.Columns.FIELD_APP_PACKAGE_NAME,
                //        appModel.getPackageName());
                values.put(AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME,
                        appModel.getFullName());
                db.insert(AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE, null, values);
            }
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    private void removeAppsFromDb(List<LaunchAppInfoModel> removedAppModels){
        SQLiteDatabase db = null;
        try {
            db = mAppsLaunchDbHelper.getWritableDatabase();
            for(LaunchAppInfoModel appModel: removedAppModels) {
                db.delete(
                        AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE,
                        AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME + " LIKE ?",
                        new String[]{appModel.getFullName()}
                );
            }
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    /*@Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(
                R.layout.fragment_launcher_apps, container, false);

        final List<UserHandle> userHandles = getUserHandles();
        if(userHandles != null) {
            mUser = userHandles.get(0);
        }
        mAppsLaunchDbHelper = new AppsLaunchDbHelper(mRecyclerView.getContext());
        //clearData();
        final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(mUser);
        final List<LaunchAppInfoModel> appsModels = synchronizeWithDb(applicationInfos);

        //Log.i("DB","COUNT " +  applicationInfos.size() + " " + appsModels.size());

        int spanCount = getRecyclerSpanCount();
        String sortType = getRecyclerSortType();
        createRecyclerView(appsModels, spanCount, sortType);

        mAppsChangeReceiver = new AppsChangeReceiver(onAppsChangeListener);
        registerAppsChangeReceiver(
                mAppsChangeReceiver,
                new String[]{
                        Intent.ACTION_PACKAGE_ADDED,
                        Intent.ACTION_PACKAGE_REPLACED,
                        Intent.ACTION_PACKAGE_REMOVED}
        );

        return mRecyclerView;
    }

    private int getRecyclerSpanCount(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mRecyclerView.getContext());
        String compactLayoutPrefKey =
                getResources().getString(R.string.compact_layout_preference_key);
        boolean isCompact = sharedPreferences.getBoolean(compactLayoutPrefKey, false);
        if(isCompact){
            return getResources().getInteger(R.integer.compact_views_count);
        } else {
            return getResources().getInteger(R.integer.views_count);
        }
    }

    private String getRecyclerSortType(){
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mRecyclerView.getContext());
        String sortTypePrefKey =
                getResources().getString(R.string.sorttype_apps_pref_key);
        return sharedPreferences.getString(sortTypePrefKey, "0");
    }

    private void clearData() {
        try {
            SQLiteDatabase db = mAppsLaunchDbHelper.getWritableDatabase();
            db.delete(AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE, null, null);
            db.close();
        } catch (SQLiteException e) {}
    }

    private List<LaunchAppInfoModel> synchronizeWithDb(List<LauncherActivityInfo>
                                                               applicationInfos) {
        LinkedList<LaunchAppInfoModel> currentAppsModels = new LinkedList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = mAppsLaunchDbHelper.getReadableDatabase();
            cursor = db.query(AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE,
                    new String[]{AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME,
                            AppsLaunchCountDb.Columns.FIELD_APP_LAUNCH_COUNT },
                    null, null, null, null, null);
            List<String> appsFullNameDb = new LinkedList<>();
            List<Integer> appsLaunchCountsDb = new LinkedList<>();
            while(cursor.moveToNext()){
                appsFullNameDb.add(cursor.getString(0));
                appsLaunchCountsDb.add(cursor.getInt(1));
            }
            appsFullNameDb = new ArrayList<>(appsFullNameDb);
            appsLaunchCountsDb = new ArrayList<>(appsLaunchCountsDb);
            for (LauncherActivityInfo appInfo : applicationInfos) {
                LaunchAppInfoModel appModel;
                if(appsFullNameDb.contains(appInfo.getName())){
                    appModel = new LaunchAppInfoModel(
                            appInfo,
                            appsLaunchCountsDb.get(appsFullNameDb.indexOf(appInfo.getName()))
                    );
                } else {
                    appModel = new LaunchAppInfoModel(appInfo, 0);
                    ContentValues values = new ContentValues();
                    values.put(AppsLaunchCountDb.Columns.FIELD_APP_LAUNCH_COUNT,
                            appModel.getLaunchCount());
                    //values.put(AppsLaunchCountDb.Columns.FIELD_APP_PACKAGE_NAME,
                    //        appModel.getPackageName());
                    values.put(AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME,
                            appModel.getFullName());
                    db.insert(AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE, null, values);
                }
                Log.i("DB",appModel.getFullName() + " " +
                        appModel.getLaunchCount());
                currentAppsModels.add(appModel);
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null)cursor.close();
            if(db != null)db.close();
        }
        return new ArrayList<>(currentAppsModels);
    }

    private @Nullable List<UserHandle> getUserHandles(){
        final UserManager userManager = (UserManager)
                mRecyclerView.getContext().getSystemService(Context.USER_SERVICE);
        return userManager != null ? userManager.getUserProfiles() : null ;
    }

    private @Nullable List<LauncherActivityInfo> getApplicationInfos(UserHandle userHandle){
        final LauncherApps launcherApps = (LauncherApps)
                mRecyclerView.getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        List<LauncherActivityInfo> applicationInfos = null;
        if(launcherApps != null)
            applicationInfos = launcherApps.getActivityList(null, userHandle);
        return applicationInfos;
    }

    private void createRecyclerView(List<LaunchAppInfoModel> appsModels,
                                    int spanCount, String sortType){
        // = findViewById(R.id.recycler_apps);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mRecyclerView.getContext(), spanCount));
        final OnRecyclerViewGestureActioner onRecyclerViewGestureActioner =
                new OnRecyclerViewGestureActioner() {
                    @Override
                    public void launchApp(Context context, LaunchAppInfoModel incrementedAppModel){
                        final LauncherApps launcherApps =
                                (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                        incrementLaunchCountDb(incrementedAppModel);
                        if(launcherApps != null) {
                            launcherApps.startMainActivity(
                                    incrementedAppModel.getComponentName(),
                                    mUser, null, null);
                        }
                    }

                    @Override
                    public void showPopup(View v, final LaunchAppInfoModel appModel){
                        final Context context = v.getContext();
                        PopupMenu popupMenu = new PopupMenu(context, v);
                        popupMenu.setOnMenuItemClickListener(createOnMenuListener(context,
                                appModel));
                        popupMenu.inflate(R.menu.apps_popup_menu);
                        MenuItem item = popupMenu.getMenu().getItem(0);
                        item.setTitle("count: " + appModel.getLaunchCount());
                        popupMenu.show();
                    }
                };
        AppsAdapter adapter = new AppsAdapter(appsModels, onRecyclerViewGestureActioner, sortType);
        mRecyclerView.setAdapter(adapter);
    }

    private void incrementLaunchCountDb(LaunchAppInfoModel incrementedAppModel){
        SQLiteDatabase db = null;
        ContentValues values = new ContentValues();
        values.put(AppsLaunchCountDb.Columns.FIELD_APP_LAUNCH_COUNT,
                incrementedAppModel.getLaunchCount());
        try {
            db = mAppsLaunchDbHelper.getWritableDatabase();
            db.update(
                    AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE,
                    values,
                    AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME + " LIKE ?",
                    new String[]{incrementedAppModel.getFullName()}
            );
        } catch (SQLiteException e) {
        } finally {
            if(db != null)db.close();
        }
    }

    /*private int getLaunchCountFromDb(String appFullName){
        return 1;
    }*/

    private PopupMenu.OnMenuItemClickListener
    createOnMenuListener(final Context context,
                         final LaunchAppInfoModel appModel){
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.popup_delete) {
                    final String deletePackageName =
                            appModel.getPackageName();
                    deleteApp(context, deletePackageName);
                    return true;
                } else if (item.getItemId() == R.id.popup_info) {
                    final ComponentName showComponentName = appModel.getComponentName();
                    final UserHandle user = mUser;
                    showAppInfo(showComponentName, user);
                    return true;
                }
                return false;
            }
        };
    }

    private void deleteApp(Context context, String packageName){
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);
    }

    private void showAppInfo(ComponentName componentName, UserHandle userHandle){
        final LauncherApps launcherApps = (LauncherApps)
                mRecyclerView.getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (launcherApps != null) {
            launcherApps.startAppDetailsActivity(componentName, userHandle,
                    null, null);
        }
    }

    private void registerAppsChangeReceiver(AppsChangeReceiver appsChangeReceiver,
                                            String[] actions){
        IntentFilter intentFilter = new IntentFilter();
        for(String action: actions) {
            intentFilter.addAction(action);
        }
        intentFilter.addDataScheme("package");
        mRecyclerView.getContext().registerReceiver(appsChangeReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.getContext().unregisterReceiver(mAppsChangeReceiver);
        mAppsLaunchDbHelper.close();
    }
}