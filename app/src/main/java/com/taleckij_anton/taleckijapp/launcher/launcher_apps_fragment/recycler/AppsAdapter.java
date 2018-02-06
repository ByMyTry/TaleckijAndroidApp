package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;
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


    private final List<LaunchAppInfoModel> mAppsData;
    private final OnRecyclerViewGestureActioner mOnRecyclerViewGestureActioner;
    private final Comparator<LaunchAppInfoModel> mComporator;

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

    public AppsAdapter(List<LaunchAppInfoModel> appsModels,
                       OnRecyclerViewGestureActioner onRecyclerViewGestureActioner,
                       String sortType){
        if(!sortType.equals(WITHOUT_SORT)){
            mComporator = getComporatorBySortType(sortType);
            Collections.sort(appsModels, getComporatorBySortType(sortType));
        } else
            mComporator = null;
        this.mAppsData = appsModels;
        this.mOnRecyclerViewGestureActioner = onRecyclerViewGestureActioner;
    }

    public void updateAfterAdd(List<LaunchAppInfoModel> addedAppModels, int uid){
        mAppsData.addAll(addedAppModels);
        notifyDataSetChanged();
    }

    public List<LaunchAppInfoModel> updateAfterRemove(int uid){
        List<LaunchAppInfoModel> removedAppModel = getRemovedAppsModelsByUid(mAppsData, uid);
        mAppsData.removeAll(removedAppModel);
        notifyDataSetChanged();
        return removedAppModel;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View appView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_launcher_apps_item, parent, false);
        return new AppViewHolder(appView);
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, int position) {
        final LaunchAppInfoModel appModel
                = mAppsData.get(holder.getAdapterPosition());
        //density 0 ~ юзает дефолтный размер иконки
        holder.bind(appModel.getIcon(0), appModel.getLabel());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appModel.incrementLaunchCount();
                if(mComporator != null){
                    Collections.sort(mAppsData, mComporator);
                    notifyDataSetChanged();
                }
                mOnRecyclerViewGestureActioner.launchApp(v.getContext(), appModel);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                mOnRecyclerViewGestureActioner.showPopup(v, appModel);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAppsData.size();
    }
}
