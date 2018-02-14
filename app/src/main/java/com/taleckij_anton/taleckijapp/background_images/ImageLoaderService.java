package com.taleckij_anton.taleckijapp.background_images;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by Lenovo on 13.02.2018.
 */

public class ImageLoaderService extends JobIntentService {
    public static final int JOB_ID_LOAD_IMAGE = 21234;

    public static final String PARAM_IMAGE_NAME = "com.taleckij_anton.taleckijapp.PARAM_IMAGE_NAME";
    public static final String ACTION_LOAD_IMAGE = "com.taleckij_anton.taleckijapp.LOAD_IMAGE";

    public static final String ACTION_UPDATE_CACHE = "com.taleckij_anton.taleckijapp.ACTION_UPDATE_CACHE";

    public static final String BROADCAST_ACTION_UPDATE_IMAGE = "com.taleckij_anton.taleckijapp.UPDATE_IMAGE";
    public static final String BROADCAST_PARAM_IMAGE = "com.taleckij_anton.taleckijapp.IMAGE";

    private static final int IMAGES_ALPHA = 120;
    private static final long TIME_INTERVAL = 15 * 60 * 1000;

    private final ImageLoader mImageLoader;
    private Thread mUpdateCacheThread;

    public ImageLoaderService() {
        mImageLoader = ImageLoader.getInstance();
    }

    public static void enqueueWork(Context context, String action, @Nullable String name) {
        Intent intent = new Intent(action);
        if(null == name){
            name = ImageSaver.DEFAULT_IMAGE_NAME;
        }
        intent.putExtra(PARAM_IMAGE_NAME, name);
        runCacheUpdate(context);
        enqueueWork(context, ImageLoaderService.class, JOB_ID_LOAD_IMAGE, intent);
    }

    private static void runCacheUpdate(Context context){
        final Intent intent = new Intent(ACTION_UPDATE_CACHE);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0);//PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        String action = intent.getAction();
        if (ACTION_LOAD_IMAGE.equals(action)) {
            /*if(mUpdateCacheThread == null) {
                mUpdateCacheThread = createUpdateCacheThread();
                mUpdateCacheThread.start();
            }*/
            String name = intent.getStringExtra(PARAM_IMAGE_NAME);
            if(ImageSaver.getInstance().isCached(name) == false){
                final String imageUrl = mImageLoader.getImageUrl();
                if (TextUtils.isEmpty(imageUrl))
                    return;
                final Bitmap bitmap = mImageLoader.loadBitmap(imageUrl);
                ImageSaver.getInstance().saveImage(getApplicationContext(),
                        changeApacity(bitmap), name);
            }
            final Intent broadcastIntent = new Intent(BROADCAST_ACTION_UPDATE_IMAGE);
            broadcastIntent.putExtra(name, true);
            sendBroadcast(broadcastIntent);
        }
    }

    private Thread createUpdateCacheThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(15000);
                        final Intent intent = new Intent(ACTION_UPDATE_CACHE);
                        sendBroadcast(intent);
                        Log.i("TAGGGG", "_----------------------------------------------------------");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
