package com.taleckij_anton.taleckijapp.background_images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Lenovo on 13.02.2018.
 */

public class ImageSaver {

    private static final Object mLock = new Object();
    private static volatile ImageSaver sInstance;
    private final CopyOnWriteArrayList<String> mCachedImageNames = new CopyOnWriteArrayList<>();

    private static final String IMAGE_DIRECTORY = "images";
    private static final int IMAGE_QUALITY = 100;

    public static final String DEFAULT_IMAGE_NAME = "default_image_name";

    public static ImageSaver getInstance(){
        if(null == sInstance){
            synchronized (mLock){
                if(null == sInstance){
                    sInstance = new ImageSaver();
                }
            }
        }

        return sInstance;
    }

    private ImageSaver(){}

    @NonNull
    private File createFile(final Context context, final String fileName){
        File directory = context.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE);

        return new File(directory, fileName);
    }

    public void saveImage(final Context context, final Bitmap bitmap, final String fileName){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(createFile(context, fileName));
            bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, fileOutputStream);
            mCachedImageNames.add(fileName);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap loadImage(final Context context, final String fileName){
        if(isCached(context, fileName)) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(createFile(context, fileName));
                return BitmapFactory.decodeStream(fileInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public boolean isCached(Context context, String fileName){
        if (mCachedImageNames.contains(fileName)) {
            return true;
        }
//        else if (createFile(context, fileName).exists()) {
//            mCachedImageNames.add(fileName);
//            return true;
//        }
        return false;
    }

    public ArrayList<String> clear(final Context context){
        final ArrayList<String> cachedImageNames = new ArrayList<>();
        for(String imageName : mCachedImageNames) {
            context.deleteFile(createFile(context, imageName).getName());
            mCachedImageNames.remove(imageName);
            cachedImageNames.add(imageName);
        }
        return cachedImageNames;
    }
}
