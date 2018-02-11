package com.taleckij_anton.taleckijapp;

import android.app.Application;

import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

/**
 * Created by Lenovo on 11.02.2018.
 */

public class App extends Application {
    public static final String METRICA_API_KEY = "618ff4f2-ec21-4dd2-a107-2d9ce63f2ccc";

    @Override
    public void onCreate() {
        super.onCreate();

        YandexMetricaConfig.Builder metricaConfigBuilder =
                YandexMetricaConfig.newConfigBuilder(METRICA_API_KEY);
        metricaConfigBuilder.handleFirstActivationAsUpdate(true);
        YandexMetrica.activate(getApplicationContext(), metricaConfigBuilder.build());
        YandexMetrica.enableActivityAutoTracking(this);
    }
}
