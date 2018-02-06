package com.taleckij_anton.taleckijapp.launcher.applications.db;

import android.provider.BaseColumns;

/**
 * Created by Lenovo on 06.02.2018.
 */

public interface AppsLaunchCountDb {
    String APPS_LUNCH_COUNT_TABLE = "APPS_LUNCH_COUNT_TABLE";

    public interface Columns extends BaseColumns {
        String FIELD_APP_LAUNCH_COUNT = "app_launch_count";
        String FIELD_APP_PACKAGE_NAME = "app_package_name";
        String FILED_APP_FULL_NAME = "app_full_name";
        //String FIELD_APP_FIRST_INSTALL_TIME = "app_first_install_time";
    }

    String CREATE_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + APPS_LUNCH_COUNT_TABLE + "(" +
                    Columns.FIELD_APP_LAUNCH_COUNT + " NUMBER," +
                    Columns.FIELD_APP_PACKAGE_NAME + " TEXT," +
                    Columns.FILED_APP_FULL_NAME + " TEXT" +
                    //Columns.FIELD_APP_FIRST_INSTALL_TIME + "NUMBER" +
                    ")";

    String DROP_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + APPS_LUNCH_COUNT_TABLE;
}
