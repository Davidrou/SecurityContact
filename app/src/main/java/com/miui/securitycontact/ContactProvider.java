package com.miui.securitycontact;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by luozhanwei on 17-7-26.
 */
public class ContactProvider extends ContentProvider {

    public static final String AUTHORITY = "com.miui.securitycontact.provider";
    private DatabaseHelper mDatabaseHelper;
    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb =new SQLiteQueryBuilder();
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        qb.setTables(PersonColumns.TABLE_NAME);
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase  db= mDatabaseHelper.getWritableDatabase();
        String name =contentValues.getAsString(PersonColumns.NAME);
        String tel = contentValues.getAsString(PersonColumns.TEL);
        String department = contentValues.getAsString(PersonColumns.DEPARTMENT);
        CryptHelper cryptHelper = CryptHelper.getInstance(getContext());
        contentValues.put(PersonColumns.NAME, cryptHelper.makeStringEncrypted(name));
        contentValues.put(PersonColumns.TEL, cryptHelper.makeStringEncrypted(tel));
        contentValues.put(PersonColumns.DEPARTMENT, cryptHelper.makeStringEncrypted(department));
        contentValues.put(PersonColumns.TEL_HASH, CryptHelper.getSHA256Digest(tel));
        long rowId = db.insert(PersonColumns.TABLE_NAME, "", contentValues);
        if (rowId > 0) {
            Uri rowUri = ContentUris.withAppendedId(PersonColumns.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public static final class PersonColumns implements BaseColumns {
        // CONTENT_URI跟数据库的表关联，最后根据CONTENT_URI来查询对应的表
        public static final Uri CONTENT_URI = Uri.parse("content://"+ AUTHORITY +"/contact");
        public static final String TABLE_NAME = "contact";

        public static final String NAME = "name";
        public static final String TEL = "tel";
        public static final String DEPARTMENT = "department";
        public static final String TEL_HASH = "tel_hash";

    }


}
