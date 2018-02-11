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
import android.support.v7.widget.LinearLayoutManager;
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
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

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
            final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
            List<LaunchAppInfoModel> removedAppsModels = adapter.updateAfterRemove(removedAppsUid);
            removeAppsFromDb(removedAppsModels);
        }
    };

    private final static String APPS_LAYOUT_TYPE_KEY = "APPS_LAYOUT_TYPE_KEY";

    public final static String APPS_GRID_LAYOUT = "APPS_GRID_LAYOUT";
    public final static String APPS_LINEAR_LAYOUT = "APPS_LINEAR_LAYOUT";

    public static AppsFragment getInstance(String layoutType){
        Bundle args = new Bundle();
        args.putString(APPS_LAYOUT_TYPE_KEY, layoutType);
        AppsFragment appsFragment = new AppsFragment();
        appsFragment.setArguments(args);
        return appsFragment;
    }

    private List<LaunchAppInfoModel> getAddedAppsModelsByUid(List<LauncherActivityInfo> appsData,
                                                             int uid){
        LinkedList<LaunchAppInfoModel> addedAppsModels = new LinkedList<>();
        for (LauncherActivityInfo appData: appsData){
            LaunchAppInfoModel appModel = new LaunchAppInfoModel(
                    appData, 0,
                    new AppViewModel(null, null)
            );
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
        final List<LaunchAppInfoModel> appModels = synchronizeWithDb(applicationInfos);
        //final List<AppViewModel> appViewModels = getAppsViewModels(appModels.size());

        //Log.i("DB","COUNT " +  applicationInfos.size() + " " + appsModels.size());

        int spanCount = getRecyclerSpanCount();
        String sortType = getRecyclerSortType();
        String layoutType = getArguments().getString(APPS_LAYOUT_TYPE_KEY, APPS_GRID_LAYOUT);
        createRecyclerView(appModels, spanCount, sortType, layoutType);

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
                            appsLaunchCountsDb.get(appsFullNameDb.indexOf(appInfo.getName())),
                            new AppViewModel(null, null)
                    );
                } else {
                    appModel = new LaunchAppInfoModel(
                            appInfo, 0,
                            new AppViewModel(null, null)
                    );
                    ContentValues values = new ContentValues();
                    values.put(AppsLaunchCountDb.Columns.FIELD_APP_LAUNCH_COUNT,
                            appModel.getLaunchCount());
                    //values.put(AppsLaunchCountDb.Columns.FIELD_APP_PACKAGE_NAME,
                    //        appModel.getPackageName());
                    values.put(AppsLaunchCountDb.Columns.FILED_APP_FULL_NAME,
                            appModel.getFullName());
                    db.insert(AppsLaunchCountDb.APPS_LUNCH_COUNT_TABLE, null, values);
                }
                currentAppsModels.add(appModel);
            }
        } catch (SQLiteException e) {
        } finally {
            if(cursor != null)cursor.close();
            if(db != null)db.close();
        }
        return new ArrayList<>(currentAppsModels);
    }

    /*private List<AppViewModel>  getAppsViewModels(int size){
        LinkedList<AppViewModel> appViewModels = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            appViewModels.add(new AppViewModel(null, null));
        }
        return appViewModels;
    }*/

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

    private void createRecyclerView(//List<AppViewModel> appViewModels,
                                    List<LaunchAppInfoModel> appModels,
                                    int spanCount, String sortType,
                                    String layoutType){
        if(layoutType.equals(APPS_GRID_LAYOUT)) {
            mRecyclerView.setLayoutManager(
                    new GridLayoutManager(mRecyclerView.getContext(), spanCount));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        }
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

                        YandexMetrica.reportEvent(MetricaAppEvents.AppOpen,
                            String.format("{\"app_name\":%s}", incrementedAppModel.getLabel()));
                    }

                    @Override
                    public void showPopup(View v, final LaunchAppInfoModel appModel){
                        final Context context = v.getContext();
                        PopupMenu popupMenu = new PopupMenu(context, v);
                        popupMenu.setOnMenuItemClickListener(createOnMenuListener(context,
                                appModel));
                        popupMenu.inflate(R.menu.apps_popup_menu);
                        MenuItem item = popupMenu.getMenu().getItem(0);
                        CharSequence curTitle = item.getTitle();
                        item.setTitle(curTitle + ": " + appModel.getLaunchCount());
                        popupMenu.show();
                    }
                };
        AppsAdapter adapter = new AppsAdapter(appModels, onRecyclerViewGestureActioner,
                sortType, layoutType);
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

    private PopupMenu.OnMenuItemClickListener createOnMenuListener(final Context context,
                         final LaunchAppInfoModel appModel){
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.popup_delete) {
                    final String deletePackageName =
                            appModel.getPackageName();
                    deleteApp(context, deletePackageName);

                    YandexMetrica.reportEvent(MetricaAppEvents.AppPackageDelete,
                            String.format("{\"package_name\":%s}", appModel.getPackageName()));

                    return true;
                } else if (item.getItemId() == R.id.popup_info) {
                    final ComponentName showComponentName = appModel.getComponentName();
                    final UserHandle user = mUser;
                    showAppInfo(showComponentName, user);

                    YandexMetrica.reportEvent(MetricaAppEvents.AppInfoOpen,
                            String.format("{\"app_name\":%s}", appModel.getLabel()));

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
