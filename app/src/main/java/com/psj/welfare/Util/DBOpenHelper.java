package com.psj.welfare.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public class DBOpenHelper
{
    private static final String DATABASE_NAME = "hyemo.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private AtomicInteger mOpenCounter = new AtomicInteger();

    public DBOpenHelper(Context context)
    {
        this.mCtx = context;
    }

    private class DatabaseHelper extends SQLiteOpenHelper
    {
        public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CreateDB._CREATE0);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    /* 서버에서 받은 토큰값을 SQLite에 저장하는 메서드
    * synchronized() 처리를 하지 않으면 첫 로그인 시 attempt to re-open an already-closed object 에러가 발생해 앱이 죽는다 */
    public long insertColumn(String token)
    {
        SQLiteDatabase sqlite = null;
        try
        {
            sqlite = mDBHelper.getWritableDatabase();
            synchronized (sqlite)
            {
                ContentValues values = new ContentValues();
                values.put(CreateDB.TOKEN, token);
                return mDB.insert(CreateDB._TABLENAME, null, values);
            }
        }
        finally
        {
            // SQLite에 저장이 끝나면 어찌됐든 SQLiteDatabase 객체를 close()해서 다른 에러가 발생하지 않게 한다
            if (sqlite != null && sqlite.isOpen())
            {
                sqlite.close();
            }
        }
    }

    public Cursor selectColumns()
    {
        return mDB.query(CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    public Cursor sortColumn(String sort)
    {
        Cursor cursor = mDB.rawQuery("SELECT * FROM token_table ORDER BY " + sort + ";", null);
        return cursor;
    }

    // UPDATE
    public boolean updateColumn(String token)
    {
        ContentValues values = new ContentValues();
        values.put(CreateDB.TOKEN, token);
        return mDB.update(CreateDB._TABLENAME, values, "_id=" + 1, null) > 0;
    }

    public synchronized DBOpenHelper open() throws SQLException
    {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public synchronized DBOpenHelper openDatabase() throws SQLException
    {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        if (mOpenCounter.incrementAndGet() == 1)
        {
            mDB = mDBHelper.getWritableDatabase();
        }
        return this;
    }

    public synchronized void closeDatabase()
    {
        if (mOpenCounter.decrementAndGet() == 0)
        {
            mDB.close();
        }
    }

    public synchronized void create()
    {
        mDBHelper.onCreate(mDB);
    }

    public void close()
    {
        mDB.close();
    }

}
