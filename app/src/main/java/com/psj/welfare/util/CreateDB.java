package com.psj.welfare.util;

import android.provider.BaseColumns;

public final class CreateDB implements BaseColumns
{
    public static final String TOKEN = "token";
    public static final String _TABLENAME = "token_table";
    public static final String _CREATE0 = "create table if not exists " + _TABLENAME
            + "(" + _ID + " integer primary key autoincrement, "
            + TOKEN + " text not null"
            + ");";
}
