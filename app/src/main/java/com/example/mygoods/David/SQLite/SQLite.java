package com.example.mygoods.David.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite extends SQLiteOpenHelper {

    // Table Name
    public static final String RECENTVIEW_TABLE   = "RECENTVIEW";
    public static final String RECENTSEARCH_TABLE = "RECENTSEARCH";

    // Table columns
    public static final String _ID          = "_id";
    public static final String ITEM_ID      = "item_id";
    public static final String DATE         = "date";

    // database version
    public static int DB_VERSION     = 1;

    // Creating table query
    private static final String RECENT_VIEW = "create table if not exists " + RECENTVIEW_TABLE + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEM_ID + " TEXT NOT NULL, " + DATE + " TEXT NOT NULL);";
    private static final String RECENT_SEARCH = "create table if not exists " + RECENTSEARCH_TABLE + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEM_ID + " TEXT NOT NULL, " + DATE + " TEXT NOT NULL);";

    // Create database
    public SQLite(Context context, String databaseName) {
        super(context, databaseName, null, DB_VERSION);
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RECENT_VIEW);
        db.execSQL(RECENT_SEARCH);
    }

    // In case you want to drop the table or upgrade the version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}