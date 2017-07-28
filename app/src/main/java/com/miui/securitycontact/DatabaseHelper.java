package com.miui.securitycontact;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "miui_ep_secure_contact.db";
    private static final int DATABASE_VERSION = 1;  
  
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    @Override  
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ContactProvider.PersonColumns.TABLE_NAME + " ("
                + ContactProvider.PersonColumns._ID + " INTEGER PRIMARY KEY,"
                + ContactProvider.PersonColumns.NAME + " TEXT,"
                + ContactProvider.PersonColumns.TEL + " TEXT,"
                + ContactProvider.PersonColumns.DEPARTMENT + " TEXT,"
                + ContactProvider.PersonColumns.TEL_HASH + " TEXT"
                + ");");  
    }  
  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("DROP TABLE IF EXISTS " + ContactProvider.PersonColumns.TABLE_NAME);
        onCreate(db);  
    }  
}  