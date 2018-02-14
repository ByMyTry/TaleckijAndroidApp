package com.taleckij_anton.taleckijapp.background_images;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.taleckij_anton.taleckijapp.LauncherActivity;

/**
 * Created by Lenovo on 14.02.2018.
 */

public class UpdateImageBroadcastReceiver extends BroadcastReceiver {

    private final Context mContext;
    //private final mDrawableCollback;

    public UpdateImageBroadcastReceiver(final Context context){
        mContext = context;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        if (ImageLoaderService.BROADCAST_ACTION_UPDATE_IMAGE.equals(action)) {
            final String imageName = intent.getStringExtra(ImageLoaderService.BROADCAST_PARAM_IMAGE);
            if(null == imageName){
                final Bitmap bitmap = ImageSaver.getInstance().loadImage(mContext);
                setDrawable(bitmap);
            } else {
                if (TextUtils.isEmpty(imageName) == false) {
                    final Bitmap bitmap = ImageSaver.getInstance()
                            .loadImage(mContext, imageName);
                    setDrawable(bitmap);
                }
            }
        }
    }

    private void setDrawable(final Bitmap bitmap){
        final Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        //mDrawableCollback.setDrawable(drawable);
    }
}
