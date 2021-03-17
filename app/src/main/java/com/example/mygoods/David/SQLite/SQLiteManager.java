package com.example.mygoods.David.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteManager {

    private SQLite dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public SQLiteManager(Context c) {
        context = c;
    }

    public SQLiteManager open() throws SQLException {
        dbHelper = new SQLite(context, "RecentView.DB");
        dbHelper = new SQLite(context, "RecentSearch.DB");
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String table, String item) {

        ContentValues contentValue = new ContentValues();

        contentValue.put(SQLite.ITEM_ID, item);
        contentValue.put(SQLite.DATE, System.currentTimeMillis());
        Cursor cursor = fetch(table);
        if (cursor != null && cursor.getCount() != 0  ) {
            do {
                String getItemID = cursor.getString(cursor.getColumnIndex("item_id"));

                if (getItemID.equals(item)) {
                    update(table, item, System.currentTimeMillis()); //update date
                    return;
                }
            } while (cursor.moveToNext());
            database.insert(table, null, contentValue);
        }else{
            database.insert(table, null, contentValue);
        }
    }

    public Cursor fetch(String table) {
        Cursor cursor = null;
        String[] columns = new String[]{SQLite._ID, SQLite.ITEM_ID, SQLite.DATE};

        String query = "SELECT * FROM "+table+" ORDER BY "+SQLite.DATE+" DESC";

        cursor = database.rawQuery(query, null);

        //cursor = database.query(table, columns, null, null, null, null, SQLite.DATE+" DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public int update(String table, String item, long date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLite.DATE, date);
        int i = database.update(table, contentValues, SQLite.ITEM_ID + "=" + "'"+item+"'", null);
        return i;
    }

    public void delete(String table, String item) {
        database.delete(table, SQLite.ITEM_ID + "=" + "'"+item+"'", null);
    }

    public void dropTable() {
        database.execSQL("DROP TABLE IF EXISTS RECENTVIEW");
        database.execSQL("DROP TABLE IF EXISTS RECENTSEARCH");
    }

    public void deleteAllRows(String table) {
        database.execSQL("DELETE FROM "+table);
    }

}
