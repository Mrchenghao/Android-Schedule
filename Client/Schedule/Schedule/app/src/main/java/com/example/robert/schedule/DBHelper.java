package com.example.robert.schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static String ddlCreateTeacher = "create table teacher" +
            "(teacherID varchar(20) primary key ,teacherName varchar(20))";

    private static String ddlCreateSchedule = "create table schedule" +
            "(id integer primary key autoincrement,teacherID varchar(20) ,classInfo varchar(256),weekday integer,fromClass integer)";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        //Log.i("robert","sql");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ddlCreateTeacher);
        db.execSQL(ddlCreateSchedule);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists teacher");
        db.execSQL("drop table if exists schedule");
        onCreate(db);
    }
}

