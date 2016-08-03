package com.example.chilin_wang.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Chilin_Wang on 2016/8/2.
 */
public class MyCreateDBTable {
    private static final String TABLE_RECORD_NAME = "table_record_name";
    private static final String RECORD_DEFAULT_ID = "record_default_id";
    private static final String RECORD_DEFAULT_NAME = "record_default_name";
    private static final String RECORD_USER_NAME = "record_user_name";
    private static final String KEY_ID = "_id";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_NUMBER = "item_number";
    private static final String ITEM_UNIT = "item_unit";
    private static final String ITEM_PRICE = "item_price";
    private static final String AVERAGE_PRICE = "average_price";
    private static final String SHOP_NAME = "shop_name";
    private String TABLE_NAME;

    private SQLiteDatabase db;

    public MyCreateDBTable(Context context) {
        db = MyDBHelper.getDatabase(context);
        if (!isTableExists(TABLE_RECORD_NAME)) {
            String CREATE_TABLE = "CREATE TABLE " + TABLE_RECORD_NAME + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RECORD_DEFAULT_ID + " INTEGER NOT NULL, " +
                    RECORD_DEFAULT_NAME + " TEXT NOT NULL, " +
                    RECORD_USER_NAME + " TEXT" +
                    ")";

            db.execSQL(CREATE_TABLE);
        }

    }

    public void close() {
        db.close();
    }

    public void openTable(String tableName) {
        TABLE_NAME = tableName;
    }

    public void createTable(int tableId, String tableName, String tableNameByUser) {
        TABLE_NAME = tableName;
        String CREATE_TABLE = "CREATE TABLE " + tableName + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ITEM_NAME + " TEXT NOT NULL, " +
                ITEM_NUMBER + " INTEGER, " +
                ITEM_UNIT + " TEXT, " +
                ITEM_PRICE + " INTEGER, " +
                AVERAGE_PRICE + " REAL, " +
                SHOP_NAME + " TEXT" +
                ")";
        db.execSQL(CREATE_TABLE);

        ContentValues cv = new ContentValues();
        cv.put(RECORD_DEFAULT_ID, tableId);
        cv.put(RECORD_DEFAULT_NAME, tableName);
        cv.put(RECORD_USER_NAME, tableNameByUser);
        db.insert(TABLE_RECORD_NAME, null, cv);
    }


    public void deleteTable(String tableName, long id) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        String where = RECORD_DEFAULT_ID + "=" + id;
        db.delete(TABLE_RECORD_NAME, where, null);
    }

    public void updateTable(long id, String tableNameByUser) {
        String where = RECORD_DEFAULT_ID + "=" + id;
        ContentValues cv = new ContentValues();
        cv.put(RECORD_USER_NAME, tableNameByUser);
        db.update(TABLE_RECORD_NAME, cv, where, null);
    }

    public boolean isTableExists(String tableName) {

        Cursor cursor = db.rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '"
                        + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void insert(String itemName, int itemNum, String itemUnit, int itemPrice, String shopName) {
        float averagePrice = 0;
        if(itemNum != 0 && itemPrice !=0) {
            averagePrice = (float) itemPrice / itemNum;
        }
        ContentValues cv = new ContentValues();
        cv.put(ITEM_NAME, itemName);
        cv.put(ITEM_NUMBER, itemNum);
        cv.put(ITEM_UNIT, itemUnit);
        cv.put(ITEM_PRICE, itemPrice);
        cv.put(AVERAGE_PRICE, averagePrice);
        cv.put(SHOP_NAME, shopName);
        db.insert(TABLE_NAME, null, cv);
    }

    public void insertToTable(String tableName, String itemName, int itemNum, String itemUnit, int itemPrice, String shopName) {
        float averagePrice = (float) itemPrice / itemNum;
        ContentValues cv = new ContentValues();
        cv.put(ITEM_NAME, itemName);
        cv.put(ITEM_NUMBER, itemNum);
        cv.put(ITEM_UNIT, itemUnit);
        cv.put(ITEM_PRICE, itemPrice);
        cv.put(AVERAGE_PRICE, averagePrice);
        cv.put(SHOP_NAME, shopName);
        db.insert(tableName, null, cv);
    }

    public boolean delete(int id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public Cursor query(int id) {
        String where = KEY_ID + "=" + id;
        return db.query(TABLE_NAME, null, where, null, null, null, null, null);
    }

    public void update(int id, String itemName, int itemNum, String itemUnit, int itemPrice, String shopName) {
        float averagePrice = (float) itemPrice / itemNum;
        ContentValues cv = new ContentValues();
        cv.put(ITEM_NAME, itemName);
        cv.put(ITEM_NUMBER, itemNum);
        cv.put(ITEM_UNIT, itemUnit);
        cv.put(ITEM_PRICE, itemPrice);
        cv.put(AVERAGE_PRICE, averagePrice);
        cv.put(SHOP_NAME, shopName);
        String where = KEY_ID + "=" + id;
        db.update(TABLE_NAME, cv, where, null);
    }

    public Cursor getData() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return cursor;
    }

    public Cursor getTableList() {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECORD_NAME, null);
        return cursor;
    }
}
