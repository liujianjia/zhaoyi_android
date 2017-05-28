package com.zhaoyi.walker.custom;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jianjia Liu on 2017/5/13.
 */
//SQLiteOpenHelper子类用于打开数据库并进行对用户搜索历史记录进行增删减除的操作
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private static String name = "records.db";
    private static Integer version = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //打开数据库，建立了一个叫records的表，里面只有一列name来存储历史记录：
        db.execSQL("create table history(id integer primary key autoincrement,name varchar(200))");
        db.execSQL("create table hospital(id integer primary key autoincrement,name varchar(200))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
