package com.taleckij_anton.taleckijapp.background_images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Xml;

import com.taleckij_anton.taleckijapp.metrica_help.MetricaAppEvents;
import com.yandex.metrica.YandexMetrica;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Lenovo on 13.02.2018.
 */

public class ImageLoader {
    private static final Object mLock = new Object();
    private static volatile ImageLoader sInstance;

    private List<String> mImageUrls = new ArrayList<>();

    private ImageLoader(){}

    public static ImageLoader getInstance(){
        if(null == sInstance){
            synchronized (mLock){
                if(null == sInstance){
                    sInstance = new ImageLoader();
                    return sInstance;
                }
            }
        }

        return sInstance;
    }

    @Nullable
    private String processEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG
                    && "img".equals(parser.getName())) {
                String bigImageUrl = findBigImageUrl(parser);
                if(bigImageUrl != null){
                    return bigImageUrl;
                }
            }
        }

        return null;
    }

    @Nullable
    private String findBigImageUrl(XmlPullParser parser) {
        for (int i = 1; i < parser.getAttributeCount(); i++) {
            if ("size".equals(parser.getAttributeName(i))) {
                if ("XXXL".equals(parser.getAttributeValue(i))) {
                    return parser.getAttributeValue(i - 1);
                }
            }
        }
        return null;
    }

    @Nullable
    public Bitmap loadBitmap(String srcUrl) {
        try {
            URL url = new URL(srcUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte [] bitmap = buffer.toByteArray();
            return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public String getImageUrl() {
        final List<String> imageUrls = getImageUrls();
        if (imageUrls.isEmpty() == false) {
            String imageUrl = imageUrls.get(0);
            imageUrls.remove(imageUrl);
            //final int index = new Random().nextInt(mImageUrls.size());
            Log.i("getImageUrl", imageUrl + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            return imageUrl;//imageUrls.get(index);
        } else {
            return null;
        }
    }

    @NonNull
    private List<String> getImageUrls() {
        if (mImageUrls.isEmpty()) {
            try {
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                final String formattedDate = dateFormat.format(calendar.getTime());

                final String stringUrl = "http://api-fotki.yandex.ru/api/podhistory/poddate;" + formattedDate + "T12:00:00Z/?limit=100";
                final URL url = new URL(stringUrl);
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    final InputStream stream = connection.getInputStream();
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    String imgUrl;
                    while ((imgUrl = processEntry(parser)) != null) {
                        mImageUrls.add(imgUrl);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            YandexMetrica.reportEvent(MetricaAppEvents.DownloadImagesUrls);
        }

        return mImageUrls;
    }
}
