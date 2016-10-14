package me.zhouzhuo.sqlhelperdemo;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import me.zhouzhuo.zzsqlhelper.ZzSqlHelper;

/**
 * Created by zz on 2016/10/13.
 */
public class DbUtils {

    private static ZzSqlHelper helper;

    private DbUtils() {
    }

    public static ZzSqlHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (DbUtils.class) {
                if (helper == null) {
                    helper = new ZzSqlHelper.Builder()
                            .setDbName("hello")
                            .setVersion(13)
//                            .addTableSql("create table if not exists student (id integer primary key , name varchar, phone varchar)")
                            .addTableEntity(TestEntity.StudentEntity.class)
                            .setUpgradeListener(new ZzSqlHelper.Builder.UpgradeListener() {
                                @Override
                                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                                    if (oldVersion != 0 && db != null && db.isOpen()) {
                                        db.execSQL("drop table studententity");
                                    }
                                }
                            })
                            .create(context);
                }
            }
        }
        return helper;
    }


}
