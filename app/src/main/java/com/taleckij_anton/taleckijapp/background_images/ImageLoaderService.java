package com.taleckij_anton.taleckijapp.background_images;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Lenovo on 13.02.2018.
 */

public class ImageLoaderService extends JobIntentService {
    public static final int JOB_ID_LOAD_IMAGE = 21234;

    public static final String PARAM_IMAGE_NAME = "com.taleckij_anton.taleckijapp.PARAM_IMAGE_NAME";
    public static final String ACTION_LOAD_IMAGE = "com.taleckij_anton.taleckijapp.LOAD_IMAGE";

    public static final String BROADCAST_ACTION_UPDATE_IMAGE = "com.taleckij_anton.taleckijapp.UPDATE_IMAGE";
    public static final String BROADCAST_PARAM_IMAGE = "com.taleckij_anton.taleckijapp.IMAGE";

    private static final int IMAGES_ALPHA = 120;

    private final ImageLoader mImageLoader;

    public ImageLoaderService() {
        mImageLoader = ImageLoader.getInstance();
    }

    public static void enqueueWork(Context context, String action, @Nullable String name) {
        Intent intent = new Intent(action);
        intent.putExtra(PARAM_IMAGE_NAME, name);
        enqueueWork(context, ImageLoaderService.class, JOB_ID_LOAD_IMAGE, intent);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        String action = intent.getAction();
        if (ACTION_LOAD_IMAGE.equals(action)) {
            String name = intent.getStringExtra(PARAM_IMAGE_NAME);
            if(null == name){
                if(ImageSaver.getInstance().isDefaultCached() == false) {
                    final String imageUrl = mImageLoader.getImageUrl();
                    if (TextUtils.isEmpty(imageUrl))
                        return;
                    final Bitmap bitmap = mImageLoader.loadBitmap(imageUrl);
                    ImageSaver.getInstance().saveImage(getApplicationContext(),
                            changeApacity(bitmap));
                }

                final Intent broadcastIntent = new Intent(BROADCAST_ACTION_UPDATE_IMAGE);
                sendBroadcast(broadcastIntent);
            } else {
                if(ImageSaver.getInstance().isCached(name) == false){
                    final String imageUrl = mImageLoader.getImageUrl();
                    if (TextUtils.isEmpty(imageUrl))
                        return;
                    final Bitmap bitmap = mImageLoader.loadBitmap(imageUrl);
                    ImageSaver.getInstance().saveImage(getApplicationContext(),
                            changeApacity(bitmap), name);
                }
                final Intent broadcastIntent = new Intent(BROADCAST_ACTION_UPDATE_IMAGE);
                broadcastIntent.putExtra(BROADCAST_PARAM_IMAGE, name);
                sendBroadcast(broadcastIntent);
            }
        }
    }

    private Bitmap changeApacity(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(IMAGES_ALPHA);
        canvas.drawBitmap(bitmap, 0, 0, alphaPaint);
        return newBitmap;
    }
}
