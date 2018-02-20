package com.taleckij_anton.taleckijapp.launcher.launcher_apps_fragment.recycler;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.taleckij_anton.taleckijapp.R;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppViewHolder extends RecyclerView.ViewHolder{
    private static final int MAX_STR_LEN = 10;

    private final ImageView mAppIcon;
    private final TextView mAppLabel;

    AppViewHolder(View itemView) {
        super(itemView);
        mAppIcon = itemView.findViewById(R.id.app_icon);
        mAppLabel = itemView.findViewById(R.id.app_label);
    }

    void bind(Drawable appIcon, CharSequence appLabel){
        mAppIcon.setImageDrawable(appIcon);
        if(appLabel.length() > MAX_STR_LEN) {
            // appLabel = ((String)appLabel).substring(0, MAX_STR_LEN + 1) + "..";
        }
        mAppLabel.setText(appLabel);
    }

    /*void bind(CharSequence appLabel){
        mAppLabel.setText(appLabel);
    }

    void bind(Drawable icon){
        mAppIcon.setImageDrawable(icon);
    }*/
}
