package com.taleckij_anton.taleckijapp.launcher.apps_db;

import android.provider.BaseColumns;

/**
 * Created by Lenovo on 06.02.2018.
 */

public interface AppsDb {
    String APPS_LUNCH_COUNT_TABLE = "APPS_LUNCH_COUNT_TABLE";
    String DESKTOP_APPS_TABLE = "DESKTOP_APPS_TABLE";

    public interface CountTableColumns extends BaseColumns {
        String _ID = "count_table_id";
        String FIELD_APP_LAUNCH_COUNT = "app_launch_count";
        //String FIELD_APP_PACKAGE_NAME = "app_package_name";
        String FILED_APP_FULL_NAME = "app_full_name";
        //String FIELD_APP_FIRST_INSTALL_TIME = "app_first_install_time";
    }

    public interface DesktopTableColumns extends BaseColumns{
        String FIELD_APP_DESKTOP_POSITION = "app_desktop_Position";
        String FIELD_APP_ID = "app_id";
    }

    String CREATE_COUNT_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + APPS_LUNCH_COUNT_TABLE + "(" +
                    CountTableColumns._ID +  " INTEGER PRIMARY KEY, " +
                    CountTableColumns.FIELD_APP_LAUNCH_COUNT + " INTEGER," +
                    //Columns.FIELD_APP_PACKAGE_NAME + " TEXT," +
                    CountTableColumns.FILED_APP_FULL_NAME + " TEXT" +
                    //Columns.FIELD_APP_FIRST_INSTALL_TIME + "NUMBER" +
            ")";

    String DROP_COUNT_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + APPS_LUNCH_COUNT_TABLE;

    String CREATE_DESKTOP_TABLE_SCRIPT =
            "CREATE TABLE IF NOT EXISTS " + DESKTOP_APPS_TABLE + "(" +
                    DesktopTableColumns.FIELD_APP_DESKTOP_POSITION + " NUMBER," +
//                    DesktopTableColumns.FIELD_APP_ID + " INTEGER REFERENCES "
//                    + APPS_LUNCH_COUNT_TABLE +"(" + CountTableColumns._ID
//                    + ") ON DELETE CASCADE "+
                    DesktopTableColumns.FIELD_APP_ID + " INTEGER," +
                    "FOREIGN KEY(" + DesktopTableColumns.FIELD_APP_ID +
                        ") REFERENCES "+ APPS_LUNCH_COUNT_TABLE +"("+ CountTableColumns._ID +")" +
                        " ON DELETE CASCADE"+
            ")";

    String DROP_DESKTOP_TABLE_SCRIPT =
            "DROP TABLE IF EXISTS " + DESKTOP_APPS_TABLE;
}
