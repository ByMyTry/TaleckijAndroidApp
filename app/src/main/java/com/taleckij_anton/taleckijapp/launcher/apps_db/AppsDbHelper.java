package com.taleckij_anton.taleckijapp.launcher.apps_db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lenovo on 06.02.2018.
 */

public class AppsDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_NAME = "apps_launch.db";

    public AppsDbHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AppsDb.CREATE_COUNT_TABLE_SCRIPT);
        db.execSQL(AppsDb.CREATE_DESKTOP_TABLE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(AppsDb.DROP_COUNT_TABLE_SCRIPT);
        db.execSQL(AppsDb.DROP_DESKTOP_TABLE_SCRIPT);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
