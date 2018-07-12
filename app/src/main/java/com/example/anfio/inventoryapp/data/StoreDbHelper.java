package com.example.anfio.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.anfio.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by anfio on 05/07/2017.
 */

public class StoreDbHelper extends SQLiteOpenHelper {

    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "store_test.db";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ITEMS_TABLE =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL," +
                        ItemEntry.COLUMN_ITEM_DESCRIPTION + " TEXT NOT NULL," +
                        ItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL," +
                        ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL," +
                        ItemEntry.COLUMN_ITEM_NAME_SUPPLIER + " TEXT NOT NULL," +
                        ItemEntry.COLUMN_ITEM_EMAIL_SUPPLIER + " TEXT NOT NULL," +
                        ItemEntry.COLUMN_ITEM_PICTURE + " TEXT NOT NULL" +
                        ");";

        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}