package com.taleckij_anton.taleckijapp;

import android.graphics.Bitmap;

import com.taleckij_anton.taleckijapp.background_images.ImageLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by Lenovo on 24.02.2018.
 */

@RunWith(RobolectricTestRunner.class)
public class ImageLoaderTest {

    private final String IMAGE_URL =
            "http://img-fotki.yandex.ru/get/909136/80038252.72/0_128881_8a015653_XXXL";
    private final ImageLoader mImageLoader = ImageLoader.getInstance();

    @Test
    public void loadUrl() {
        String url = mImageLoader.getImageUrl();
        Boolean urlNotEmpty = url != null && url.length() > 0;
        assertEquals(true, urlNotEmpty);
    }

    @Test
    public void loadBitmap() {
        Bitmap bitmap = mImageLoader.loadBitmap(IMAGE_URL);
        Boolean bitmapNotEmpty = bitmap != null && bitmap.getByteCount() > 0;
        assertEquals(true, bitmapNotEmpty);
    }
}
