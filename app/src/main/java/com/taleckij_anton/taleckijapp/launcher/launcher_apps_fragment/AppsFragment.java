package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbHelper;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbSynchronizer;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.DesktopFragment;
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
    private AppsDbHelper mAppsDbHelper;
    private AppsDbSynchronizer mAppsDbSynchronizer;

    private final OnAppsChangeListener
            onAppsChangeListener = new OnAppsChangeListener() {
        @Override
        public void onAppInstalled(Context context, int addedAppsUid) {
            final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(mUser);
            final List<AppInfoModel> addedAppModels =
                    getAddedAppsModelsByUid(applicationInfos, addedAppsUid);
            final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
            adapter.updateAfterAdd(addedAppModels, addedAppsUid);
            mAppsDbSynchronizer.addAppsToDb(mAppsDbHelper, addedAppModels);
        }

        @Override
        public void onAppRemoved(Context context, int removedAppsUid) {
            final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
            List<AppInfoModel> removedAppsModels = adapter.updateAfterRemove(removedAppsUid);
            mAppsDbSynchronizer.removeAppsFromDb(mAppsDbHelper, removedAppsModels);

            sendUpdateDesktopBroadcast(context);
        }
    };

    private final BroadcastReceiver UpdateAppPosDesktopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DesktopFragment.UPDATE_APP_POS_DESK_BROADCAST_ACTION.equals(intent.getAction())){
                String appFullName = intent.getStringExtra(DesktopFragment.APP_FULL_NAME_EXTRA);
                int currentDeskPos = intent.getIntExtra(DesktopFragment.APP_DESK_POS_EXTRA, -1);
                final AppsAdapter adapter = ((AppsAdapter)mRecyclerView.getAdapter());
                //int currentDeskPos =
                //mAppsDbSynchronizer.getCurrentDeskPos(mAppsDbHelper, appFullName);
                adapter.updateAppPosFromDesk(appFullName,
                        currentDeskPos == -1 ? null : currentDeskPos);
                //Log.i("STOPDRAG", "" + currentDeskPos);
            }

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

    private List<AppInfoModel> getAddedAppsModelsByUid(List<LauncherActivityInfo> appsData,
                                                       int uid){
        LinkedList<AppInfoModel> addedAppsModels = new LinkedList<>();
        for (LauncherActivityInfo appData: appsData){
            AppInfoModel appModel = new AppInfoModel(
                    appData,
                    0,
                    new AppViewModel(null, null),
                    null
            );
            if(appModel.getUid() == uid) {
                addedAppsModels.add(appModel);
            }
        }
        return new ArrayList<>(addedAppsModels);
    }

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
        mAppsDbHelper = new AppsDbHelper(mRecyclerView.getContext());
        mAppsDbSynchronizer = AppsDbSynchronizer.getInstance();
        //clearData();

        final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(mUser);
        final List<AppInfoModel> appModels =
                mAppsDbSynchronizer.synchronizeWithDb(mAppsDbHelper, applicationInfos);
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
                        Intent.ACTION_PACKAGE_REMOVED
                }
        );

        return mRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DesktopFragment.UPDATE_APP_POS_DESK_BROADCAST_ACTION);
        mRecyclerView.getContext().registerReceiver(UpdateAppPosDesktopReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRecyclerView.getContext().unregisterReceiver(UpdateAppPosDesktopReceiver);
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
                                    List<AppInfoModel> appModels,
                                    int spanCount, String sortType,
                                    String layoutType){
        if(layoutType.equals(APPS_GRID_LAYOUT)) {
            mRecyclerView.setLayoutManager(
                    new GridLayoutManager(mRecyclerView.getContext(), spanCount));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        }
        final OnAppsViewGestureActioner onAppsViewGestureActioner =
                new OnAppsViewGestureActioner() {
                    @Override
                    public void launchApp(Context context, AppInfoModel incrementedAppModel){
                        final LauncherApps launcherApps =
                                (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                        mAppsDbSynchronizer.incrementLaunchCountDb(mAppsDbHelper, incrementedAppModel);
                        if(launcherApps != null) {
                            launcherApps.startMainActivity(
                                    incrementedAppModel.getComponentName(),
                                    mUser, null, null);
                        }

                        YandexMetrica.reportEvent(MetricaAppEvents.AppOpen,
                            String.format("{\"app_name\":%s}", incrementedAppModel.getLabel()));
                    }

                    @Override
                    public void showPopup(View v, final AppInfoModel appModel){
                        final Context context = v.getContext();
                        PopupMenu popupMenu = new PopupMenu(context, v);
                        popupMenu.setOnMenuItemClickListener(createOnMenuListener(context,
                                appModel));
                        popupMenu.inflate(R.menu.apps_popup_menu);
                        MenuItem item = popupMenu.getMenu().getItem(0);
                        CharSequence curTitle = item.getTitle();

                        boolean isDesktopApp = appModel.getDesktopPosition() != null;
                        /*        isDesktopApp(appModel.getFullName());
                        Log.i("DeskPos==null", String.valueOf());
                        Log.i("!isDeskApp", String.valueOf(!isDeskApp));*/
                        popupMenu.getMenu().getItem(1).setVisible(!isDesktopApp);
                        popupMenu.getMenu().getItem(2).setVisible(isDesktopApp);

                        item.setTitle(curTitle + ": " + appModel.getLaunchCount());
                        popupMenu.show();
                    }
                };
        AppsAdapter adapter = new AppsAdapter(appModels, onAppsViewGestureActioner,
                sortType, layoutType);
        mRecyclerView.setAdapter(adapter);
    }

    private PopupMenu.OnMenuItemClickListener createOnMenuListener(final Context context,
                         final AppInfoModel appModel){
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.popup_delete) {
                    final String deletePackageName = appModel.getPackageName();
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
                } else if (item.getItemId() == R.id.popup_add_to_desk) {
                    final String appName = appModel.getFullName();

                    final int firstAvailableDeskPos = mAppsDbSynchronizer.getFirstAvailableDeskPos(mAppsDbHelper);
                    if(firstAvailableDeskPos != -1){
                        mAppsDbSynchronizer.addToDesktopDb(mAppsDbHelper, firstAvailableDeskPos, appName);
                        appModel.setDesktopPosition(firstAvailableDeskPos);
                    } else {
                        String desktopCompletedMessage =
                                getResources().getString(R.string.desktop_filled_message);
                        Toast.makeText(context, desktopCompletedMessage, Toast.LENGTH_SHORT)
                                .show();
                    }

                    sendUpdateDesktopBroadcast(context);

                    return true;
                } else if (item.getItemId() == R.id.popup_remove_from_desk) {
                    int deskPos = appModel.getDesktopPosition() != null ?
                            appModel.getDesktopPosition() : -1;
                    mAppsDbSynchronizer.removeFromDesktopDb(mAppsDbHelper, deskPos);
                    appModel.setDesktopPosition(null);

                    sendUpdateDesktopBroadcast(context);

                    return true;
                }
                return false;
            }
        };
    }

    private void sendUpdateDesktopBroadcast(Context context){
        Intent intent = new Intent(DesktopFragment.UPDATE_DESKTOP_BROADCAST_ACTION);
        context.sendBroadcast(intent);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.getContext().unregisterReceiver(mAppsChangeReceiver);
        mAppsDbHelper.close();
    }
}
