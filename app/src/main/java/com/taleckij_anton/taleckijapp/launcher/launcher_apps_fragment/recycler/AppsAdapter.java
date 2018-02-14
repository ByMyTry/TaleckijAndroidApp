package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.recycler;

import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppViewModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppsFragment;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.LaunchAppInfoModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.OnRecyclerViewGestureActioner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppsAdapter extends RecyclerView.Adapter<AppViewHolder>{
    public static final String WITHOUT_SORT = "0";
    public static final String SORT_AZ = "1";
    public static final String SORT_ZA = "2";
    public static final String SORT_FIRST_INSTALL_TIME = "3";
    public static final String SORT_LAUNCH_COUNT = "4";


    private final List<LaunchAppInfoModel> mAppModels;
    private Thread mThread;
    private final Handler mHandler = new Handler();
    private final OnRecyclerViewGestureActioner mOnRecyclerViewGestureActioner;
    private final Comparator<LaunchAppInfoModel> mComporator;
    private final int mItemLayoutId;

    private List<LaunchAppInfoModel> getRemovedAppsModelsByUid(
            List<LaunchAppInfoModel> appsModels, int uid){
        LinkedList<LaunchAppInfoModel> removedAppsModels = new LinkedList<>();
        for (LaunchAppInfoModel appModel: appsModels){
            if(appModel.getUid() == uid) {
                removedAppsModels.add(appModel);
            }
        }
        return new ArrayList<>(removedAppsModels);
    }

    private Comparator<LaunchAppInfoModel> getComporatorBySortType(String sortType){
        if(sortType.equals(SORT_AZ)){
            return new Comparator<LaunchAppInfoModel>() {
                @Override
                public int compare(LaunchAppInfoModel o1, LaunchAppInfoModel o2) {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            };
        } else if(sortType.equals(SORT_ZA)){
            return new Comparator<LaunchAppInfoModel>() {
                @Override
                public int compare(LaunchAppInfoModel o1, LaunchAppInfoModel o2) {
                    return o2.getLabel().compareTo(o1.getLabel());
                }
            };
        } else if(sortType.equals(SORT_LAUNCH_COUNT)){
            return new Comparator<LaunchAppInfoModel>() {
                @Override
                public int compare(LaunchAppInfoModel o1, LaunchAppInfoModel o2) {
                    return Integer.compare(o2.getLaunchCount(), o1.getLaunchCount());
                }
            };
        } else { //SORT_FIRST_INSTALL_TIME
            return new Comparator<LaunchAppInfoModel>() {
                @Override
                public int compare(LaunchAppInfoModel o1, LaunchAppInfoModel o2) {
                    return Long.compare(o1.getFirstInstallTime(), o2.getFirstInstallTime());
                }
            };
        }
    }

    private int getItemLayoutIdFor(String layoutType){
        if(layoutType.equals(AppsFragment.APPS_GRID_LAYOUT)){
            return R.layout.launcher_apps_grid_item;
        } else if(layoutType.equals(AppsFragment.APPS_LINEAR_LAYOUT)) {
            return R.layout.launcher_apps_linear_item;
        }
        return -1;
    }

    public AppsAdapter(List<LaunchAppInfoModel> appsModels,
                       OnRecyclerViewGestureActioner onRecyclerViewGestureActioner,
                       String sortType, String layoutType){
        if(!sortType.equals(WITHOUT_SORT)){
            mComporator = getComporatorBySortType(sortType);
            Collections.sort(appsModels, getComporatorBySortType(sortType));
        } else
            mComporator = null;
        this.mAppModels = appsModels;
        this.mOnRecyclerViewGestureActioner = onRecyclerViewGestureActioner;
        this.mItemLayoutId = getItemLayoutIdFor(layoutType);
        fetchUiItemsAsync();
    }

    public void updateAfterAdd(List<LaunchAppInfoModel> addedAppModels, int uid){
        mAppModels.addAll(addedAppModels);
        if(mComporator != null) {
            Collections.sort(mAppModels, mComporator);
        }
        fetchUiItemsAsync();
        notifyDataSetChanged();
    }

    public List<LaunchAppInfoModel> updateAfterRemove(int uid){
        List<LaunchAppInfoModel> removedAppModel = getRemovedAppsModelsByUid(mAppModels, uid);
        mAppModels.removeAll(removedAppModel);
        fetchUiItemsAsync();
        notifyDataSetChanged();
        return removedAppModel;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View appView = LayoutInflater.from(parent.getContext())
                .inflate(mItemLayoutId, parent, false);
        return new AppViewHolder(appView);
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final LaunchAppInfoModel appModel = mAppModels.get(holder.getAdapterPosition());
        //density 0 ~ юзает дефолтный размер иконки
        //holder.bind(appModel.getIcon(0), appModel.getLabel());
        //final AppViewModel appViewModel = mAppViewModels.get(position);
        final AppViewModel appViewModel = appModel.getAppViewModel();

        if(appViewModel.getIcon() != null && appViewModel.getLabel() != null){
            holder.bind(appViewModel.getIcon(), appViewModel.getLabel());
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appModel.incrementLaunchCount();
                if(mComporator != null){
                    Collections.sort(mAppModels, mComporator);
                    notifyDataSetChanged();
                }
                mOnRecyclerViewGestureActioner.launchApp(v.getContext(), appModel);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                mOnRecyclerViewGestureActioner.showPopup(v, appModel);
                return true;
            }
        });
    }

    private void fetchUiItemsAsync(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final int size = mAppModels.size();
                for (int i = 0; i < size; i++) {
                    final LaunchAppInfoModel appModel = mAppModels.get(i);
                    if(appModel.getAppViewModel().getLabel() == null){
                        final String label = appModel.getLabel();
                        appModel.getAppViewModel().setLabel(label);
                    }
                    if(appModel.getAppViewModel().getIcon() == null) {
                        final Drawable icon = appModel.getIcon(0);
                        appModel.getAppViewModel().setIcon(icon);
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
        mThread.start();
    }

    @Override
    public int getItemCount() {
        return mAppModels.size();
    }
}
