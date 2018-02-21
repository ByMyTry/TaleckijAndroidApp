package com.taleckij_anton.taleckijapp.launcher.desktop_fragment.recycler;

import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.taleckij_anton.taleckijapp.R;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.DesktopFragment;
import com.taleckij_anton.taleckijapp.launcher.desktop_fragment.OnDeskAppsViewGestureActioner;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppInfoModel;
import com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.AppViewModel;

import java.util.List;

/**
 * Created by Lenovo on 19.02.2018.
 */

public class DesktopAppsAdapter extends RecyclerView.Adapter<DesktopAppViewHolder> {
    private Thread mThread;
    private final Handler mHandler = new Handler();
    private final List<AppInfoModel> mAppModels;
    private final OnDeskAppsViewGestureActioner mOnDeskAppsViewGestureActioner;

    private AppInfoModel mCurrentDragApp;

    private AppInfoModel getCurrentDragApp(){
        return mCurrentDragApp;
    }

    public DesktopAppsAdapter(List<AppInfoModel> appsModels,
                       OnDeskAppsViewGestureActioner onDeskAppsViewGestureActioner){
        this.mAppModels = appsModels;
        this.mOnDeskAppsViewGestureActioner = onDeskAppsViewGestureActioner;
        fetchUiItemsAsync();
    }

    @Override
    public DesktopAppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View desktopAppView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.launcher_desktop_item, parent, false);
        return new DesktopAppViewHolder(desktopAppView);
    }

    @Override
    public void onBindViewHolder(final DesktopAppViewHolder holder, int position) {
        AppInfoModel appModel = null;
        for(AppInfoModel appInfoModel : mAppModels) {
            if(appInfoModel.getDesktopPosition() != null
                    && appInfoModel.getDesktopPosition() == position) {
                appModel = appInfoModel;
            }
        }

        if(appModel != null) {
            final AppInfoModel fAppModel = appModel;
            final AppViewModel appViewModel = appModel.getAppViewModel();

            if (appViewModel.getIcon() != null && appViewModel.getLabel() != null) {
                holder.bind(appViewModel.getIcon(), appViewModel.getLabel());
            }

            holder.getAppView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fAppModel.incrementLaunchCount();
                    mOnDeskAppsViewGestureActioner.launchApp(v.getContext(), fAppModel);
                }
            });

            holder.getAppView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    //v.setVisibility(View.INVISIBLE);

                    mOnDeskAppsViewGestureActioner.startDrag(v, fAppModel);
                    mCurrentDragApp = fAppModel;
                    return true;
                }
            });
        } else {
            holder.bind(null, null);

            holder.getAppView().setOnClickListener(null);
            holder.getAppView().setOnLongClickListener(null);
        }

        holder.getContainer().setOnDragListener(new View.OnDragListener() {
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
                        if(isDeskPosAvailable(holder.getAdapterPosition())) {
                            AppInfoModel currentDragApp = getCurrentDragApp();
                            currentDragApp.setDesktopPosition(holder.getAdapterPosition());
                            mOnDeskAppsViewGestureActioner.stopDrag(v, currentDragApp);
                            notifyDataSetChanged();
                        }
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setBackground(normalShape);
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private boolean isDeskPosAvailable(int pos){
        for(AppInfoModel appInfoModel : mAppModels){
            if(appInfoModel.getDesktopPosition() != null
                    && appInfoModel.getDesktopPosition() == pos){
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return DesktopFragment.DESKTOP_APPS_TOTAL_COUNT;
    }

    private void fetchUiItemsAsync(){
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final int size = mAppModels.size();
                for (int i = 0; i < size; i++) {
                    final AppInfoModel appModel = mAppModels.get(i);
                    if(appModel.getAppViewModel().getLabel() == null) {
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
}
