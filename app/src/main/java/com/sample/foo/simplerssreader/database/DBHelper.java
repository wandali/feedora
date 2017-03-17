package com.sample.foo.simplerssreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sample.foo.simplerssreader.database.FolderContract.FolderEntry;
import com.sample.foo.simplerssreader.database.FeedContract.FeedEntry;

public class DBHelper extends SQLiteOpenHelper {
    // Update when changing scheme.
    static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "FeedReader.db";

    private static final String SQL_CREATE_FOLDERS_TABLE =
            "CREATE TABLE " + FolderEntry.TABLE_NAME + " (" +
                    FolderEntry._ID + " INTEGER PRIMARY KEY," +
                    FolderEntry.TITLE + " TEXT)";

    private static final String SQL_CREATE_FEEDS_TABLE =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.TITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FolderEntry.TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FOLDERS_TABLE);
        db.execSQL(SQL_CREATE_FEEDS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}