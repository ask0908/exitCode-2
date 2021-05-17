package com.psj.welfare;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CategoryData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract CategoryDao getcategoryDao();

    //Room Database Singleton
    private static final String DB_NAME = "Firstcategory";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context)
    {
        if (instance == null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
            //allowMainThreadQueries() =>이걸 추가해서 AsyncTask를 사용안하고 간편하게할수있지만 오류가많아 실제 앱을 만들때 사용하면 안됨
        }
        return instance;
    }

    //디비객체제거
    public static void destroyInstance()
    {
        instance = null;
    }

//    public abstract AppDatabase appDatabase();
}
