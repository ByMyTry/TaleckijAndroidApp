package com.taleckij_anton.taleckijapp.launcher.applications.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppsLaunchDbHelper extends SQLiteOpenHelper {
    static final int VERSION = 1;
    static final String DB_NAME = "apps_launch.db";

    public AppsLaunchDbHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppsLaunchCountDb.CREATE_TABLE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(AppsLaunchCountDb.DROP_TABLE_SCRIPT);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
