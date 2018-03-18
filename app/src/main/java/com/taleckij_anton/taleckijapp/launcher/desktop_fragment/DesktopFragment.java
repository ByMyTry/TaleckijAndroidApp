package com.taleckij_anton.taleckijapp.launcher.desktop_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbHelper;
import com.taleckij_anton.taleckijapp.launcher.apps_db.AppsDbSynchronizer;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.recycler.DesktopAppsAdapter;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;
import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import java.util.List;

/**
 * Created by Lenovo on 19.02.2018.
 */

public class DesktopFragment extends Fragment {
    private View mGarbageSpace;
    private RecyclerView mRecyclerView;
    private @Nullable UserHandle mUser;
    private AppsDbHelper mAppsDbHelper;
    private AppsDbSynchronizer mAppsDbSynchronizer;

    private AppInfoModel mCurrentDragApp;

    private final BroadcastReceiver UpdateDesktopBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UPDATE_DESKTOP_BROADCAST_ACTION.equals(intent.getAction())) {
                //TODO По фасту, для зачета. Сделать оптимальнее чем убрать все, добавить все
                ((DesktopAppsAdapter) mRecyclerView.getAdapter())
                        .updateDesktopHard(getCurrentDeskApps(context));
            }
        }
    };

    public static final String UPDATE_DESKTOP_BROADCAST_ACTION = "UPDATE_DESKTOP_BROADCAST_ACTION";
    public static final String UPDATE_APP_POS_DESK_BROADCAST_ACTION =
            "UPDATE_APP_POS_DESK_BROADCAST_ACTION";
    public static final String APP_FULL_NAME_EXTRA = "APP_FULL_NAME_EXTRA";
    public static final String APP_DESK_POS_EXTRA = "APP_DESK_POS_EXTRA";

    public static final int DESKTOP_APPS_TOTAL_COUNT = 6;
    public static final int DESKTOP_APPS_ROW_COUNT = 3;
    //public static final int DESKTOP_APPS_ROW_COUNT_PORT = 3;
    //public static final int DESKTOP_APPS_ROW_COUNT_LAND = 4;

    private final View.OnDragListener mGarbageDragListener = new View.OnDragListener() {
        private Drawable enterShape;
        private Drawable normalShape;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            enterShape = v.getResources().getDrawable(R.drawable.shape_droptarget,
                    v.getContext().getTheme());
            int action = event.getAction();
            switch (action){
                case DragEvent.ACTION_DRAG_ENTERED:
                    normalShape = v.getBackground();
                    v.setBackground(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackground(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    if(mCurrentDragApp.getDesktopPosition() != null) {
                        int deskPos = mCurrentDragApp.getDesktopPosition();
                        mCurrentDragApp.setDesktopPosition(null);
                        mRecyclerView.getAdapter().notifyItemChanged(deskPos);
                        mAppsDbSynchronizer.removeFromDesktopDb(mAppsDbHelper, deskPos);

                        sendUpdateAppPosFromDeskBroadcast(mCurrentDragApp);
                    }
                    mGarbageSpace.setVisibility(View.INVISIBLE);
                    v.setBackground(normalShape);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private  void sendUpdateAppPosFromDeskBroadcast(AppInfoModel appInfoModel){
        Intent intent = new Intent(UPDATE_APP_POS_DESK_BROADCAST_ACTION);
        intent.putExtra(APP_FULL_NAME_EXTRA, appInfoModel.getFullName());
        intent.putExtra(APP_DESK_POS_EXTRA,
                appInfoModel.getDesktopPosition() == null ?
                -1 : appInfoModel.getDesktopPosition());
        Log.i("STOPDRAG", "" + appInfoModel.getDesktopPosition());
        mRecyclerView.getContext().sendBroadcast(intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        View desktopView =
                inflater.inflate(R.layout.fragment_launcher_desktop, container, false);

        mGarbageSpace = desktopView.findViewById(R.id.desktop_garbage_space);
        mGarbageSpace.setOnDragListener(mGarbageDragListener);

        mRecyclerView = desktopView.findViewById(R.id.desktop_apps_recycler);

        mAppsDbHelper = new AppsDbHelper(desktopView.getContext());
        mAppsDbSynchronizer = AppsDbSynchronizer.getInstance();

        final List<UserHandle> userHandles = getUserHandles(desktopView.getContext());
        if(userHandles != null) {
            mUser = userHandles.get(0);
        }

        final List<AppInfoModel> deskAppModels = getCurrentDeskApps(getContext());
        createRecyclerView(deskAppModels, DESKTOP_APPS_ROW_COUNT);

//        desktopView.setOnDragListener();

        IntentFilter filter = new IntentFilter(UPDATE_DESKTOP_BROADCAST_ACTION);
        mRecyclerView.getContext().registerReceiver(UpdateDesktopBroadcast, filter);

        return desktopView;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        IntentFilter filter = new IntentFilter(UPDATE_DESKTOP_BROADCAST_ACTION);
//        mRecyclerView.getContext().registerReceiver(UpdateDesktopBroadcast, filter);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mRecyclerView.getContext().unregisterReceiver(UpdateDesktopBroadcast);
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.getContext().unregisterReceiver(UpdateDesktopBroadcast);
        mAppsDbHelper.close();
    }

    private List<AppInfoModel> getCurrentDeskApps(Context context){
        final List<LauncherActivityInfo> applicationInfos = getApplicationInfos(context, mUser);
        return mAppsDbSynchronizer.synchronizeWithDb(mAppsDbHelper, applicationInfos);
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
                    public void startDrag(View v, AppInfoModel appModel) {
                        mGarbageSpace.setVisibility(View.VISIBLE);
                        mCurrentDragApp = appModel;
                    }

                    @Override
                    public void stopDrag(View v, AppInfoModel appModelWithNewDescPos,
                                         boolean changeDeskPos) {
                        if(changeDeskPos) {
                            mAppsDbSynchronizer.updateDeskPosDb(mAppsDbHelper, mCurrentDragApp);
                            sendUpdateAppPosFromDeskBroadcast(mCurrentDragApp);
                        }
                    }
                };
        DesktopAppsAdapter adapter =
                new DesktopAppsAdapter(appModels, onDeskAppsViewGestureActioner);
        mRecyclerView.setAdapter(adapter);
    }
}
