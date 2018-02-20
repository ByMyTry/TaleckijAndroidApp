package com.taleckij_anton.taleckijapp.launcher.desktop_fragment.recycler;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.taleckij_anton.taleckijapp.R;

/**
 * Created by Lenovo on 19.02.2018.
 */

public class DesktopAppViewHolder extends RecyclerView.ViewHolder {
    private final View mContainer;
    private final View mAppView;
    private final ImageView mAppIcon;
    private final TextView mAppLabel;

    private static final int MAX_STR_LEN = 10;

    public View getContainer() {
        return mContainer;
    }

    public View getAppView(){
        return mAppView;
    }

    public DesktopAppViewHolder(View itemView) {
        super(itemView);
        mContainer = itemView.findViewById(R.id.desktop_item_container);
        mAppView = itemView.findViewById(R.id.desktop_item_app_view);
        mAppIcon = itemView.findViewById(R.id.desktop_item_icon);
        mAppLabel = itemView.findViewById(R.id.desktop_item_label);
    }

    void bind(@Nullable Drawable appIcon, @Nullable CharSequence appLabel){
        mAppIcon.setImageDrawable(appIcon);
        /*if(appLabel.length() > MAX_STR_LEN) {
            // appLabel = ((String)appLabel).substring(0, MAX_STR_LEN + 1) + "..";
        }*/
        mAppLabel.setText(appLabel);
    }

    void bind(@Nullable Drawable appIcon){
        mAppIcon.setImageDrawable(appIcon);
    }
}

