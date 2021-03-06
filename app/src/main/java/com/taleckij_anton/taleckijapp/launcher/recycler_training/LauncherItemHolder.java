package com.taleckij_anton.taleckijapp.launcher.recycler_training;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.taleckij_anton.taleckijapp.R;

/**
 * Created by Lenovo on 03.02.2018.
 */

public class LauncherItemHolder extends RecyclerView.ViewHolder {
    private final TextView mItemTextView;
    private final View mItemIcon;

    LauncherItemHolder(View itemView) {
        super(itemView);
        mItemTextView = itemView.findViewById(R.id.launcher_item_text);
        mItemIcon = itemView.findViewById(R.id.launcher_item_icon);
    }

    void bind(RecyclerItem recyclerItem){
        mItemTextView.setText(recyclerItem.getText());

        if(recyclerItem.getColor() == null){
            recyclerItem.setColor(RecyclerItem.getRandomColor());
        }
        mItemIcon.setBackgroundColor(recyclerItem.getColor());
    }

    public String getText(){
        return mItemTextView.getText().toString();
    }
}
