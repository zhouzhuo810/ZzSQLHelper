package me.zhouzhuo.zzsqlhelper;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.zhouzhuo.zzsqlhelper.utils.CursorUtils;
import me.zhouzhuo.zzsqlhelper.utils.Logger;

/**
 * Created by zz on 2016/10/13.
 */
public class ZzSqlHelper extends SQLiteOpenHelper {

    private Builder.UpgradeListener listener;
    private SQLiteDatabase mDb;
    private List<String> sql;

    private ZzSqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, SQLiteDatabase mDb) {
        super(context, name, factory, version);
    }

    private ZzSqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler, SQLiteDatabase mDb) {
        super(context, name, factory, version, errorHandler);
    }

    private ZzSqlHelper(Context context, String name, List<String> sql, List<Class<?>> tableEntity,
                        SQLiteDatabase.CursorFactory factory, int newVersion, Builder.UpgradeListener listener) {
        super(context, name, factory, newVersion);
        mDb = context.openOrCreateDatabase(name, 0, null);
        int oldVersion = mDb.getVersion();
        mDb.setVersion(newVersion);
        if (oldVersion != newVersion) {
            Logger.d("onUpgrade: oldVersion = "+oldVersion + ", newVersion = " + newVersion);
            if (listener != null) {
                listener.onUpgrade(mDb, oldVersion, newVersion);
            }
        }
        this.sql = sql;
        this.listener = listener;
        if (sql != null && sql.size() > 0) {
            try {
                if (Build.VERSION.SDK_INT >= 16 && mDb.isWriteAheadLoggingEnabled()) {
                    mDb.beginTransactionNonExclusive();
                } else {
                    mDb.beginTransaction();
                }
                for (String s : sql) {
                    mDb.execSQL(s);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }
        }
        if (tableEntity != null && tableEntity.size() > 0) {
            try {
                if (Build.VERSION.SDK_INT >= 16 && mDb.isWriteAheadLoggingEnabled()) {
                    mDb.beginTransactionNonExclusive();
                } else {
                    mDb.beginTransaction();
                }
                for (Class<?> clz : tableEntity) {
                    mDb.execSQL(new SQLBuilder()
                            .from(clz)
                            .createTable()
                            .build());
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class Builder {
        private int version;
        private String dbName;
        private List<String> tableSql;
        private UpgradeListener listener;
        private List<Class<?>> tableEntity;

        public Builder() {
            tableSql = new ArrayList<>();
            tableEntity = new ArrayList<>();
        }

        public Builder setVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder setDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        public Builder setUpgradeListener(UpgradeListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder addTableSql(String tableSql) {
            this.tableSql.add(tableSql);
            return this;
        }

        public Builder addTableEntity(Class<?> clz) {
            this.tableEntity.add(clz);
            return this;
        }

        public ZzSqlHelper create(Context context) {
            return new ZzSqlHelper(context, dbName, tableSql, tableEntity, null, version, listener);
        }

        public interface UpgradeListener {
            void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
        }
    }

    public void execSQL(String sql) {
        mDb.execSQL(sql);
    }

    public Cursor findById(String tableName, int id) {
        Cursor cursor = mDb.query(tableName, null, "id = ?", new String[]{id + ""}, null, null, null, null);
        CursorUtils.print(cursor);
        return cursor;
    }

    public Cursor findAll(String tableName, WhereBuilder b, String orderBy, String limit) {
        Cursor cursor =  mDb.query(tableName, null, b.getWb(), b.getVb(), null, null, orderBy, limit);
        CursorUtils.print(cursor);
        return cursor;
    }

    public Cursor findAll(String tableName, WhereBuilder b, String orderBy) {
        Cursor cursor =  mDb.query(tableName, null, b.getWb(), b.getVb(), null, null, orderBy, null);
        CursorUtils.print(cursor);
        return cursor;
    }

    public Cursor findAll(String tableName, WhereBuilder b) {
        Cursor cursor =  mDb.query(tableName, null, b.getWb(), b.getVb(), null, null, null, null);
        CursorUtils.print(cursor);
        return cursor;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public <T> T findById(Class<T> clazz, int id) {
        Cursor cursor = findById(clazz.getSimpleName(), id);
        if (cursor.moveToFirst()) {
            try {
                T t = CursorUtils.getEntity(clazz, cursor);
                if (!cursor.isClosed())
                    cursor.close();
                return t;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        return null;
    }

    public void save(Object object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();
        StringBuilder columnsAndOptions = new StringBuilder();
        StringBuilder values = new StringBuilder();
        SQLBuilder b = new SQLBuilder()
                .from(object.getClass());
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            String name = declaredField.getName();
            if (!name.equals("id") && !name.equals("$change") && !name.equals("serialVersionUID")) {
                String type = declaredField.getType().getSimpleName().toLowerCase();
                switch (type) {
                    case "string":
                        String value2 = null;
                        try {
                            value2 = (String) declaredField.get(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append("'").append(value2).append("'").append(",");
                        break;
                    case "int":
                        int value = 0;
                        try {
                            value = declaredField.getInt(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append(value).append(",");
                        break;
                    case "boolean":
                        boolean value1 = false;
                        try {
                            value1 = declaredField.getBoolean(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append("'").append(value1 ? "1" : "0").append("'").append(",");
                        break;
                    case "float":
                        float value3 = 0;
                        try {
                            value3 = declaredField.getFloat(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append(value3).append(",");
                        break;
                    case "long":
                        long value4 = 0;
                        try {
                            value4 = declaredField.getLong(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append("'").append(value4).append("'").append(",");
                        break;
                    case "double":
                        double value5 = 0;
                        try {
                            value5 = declaredField.getDouble(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append("'").append(value5).append("'").append(",");
                        break;
                    case "short":
                        short value6 = 0;
                        try {
                            value6 = declaredField.getShort(object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        columnsAndOptions.append(name).append(",");
                        values.append("'").append(value6).append("'").append(",");
                        break;

                }
            }
        }
        columnsAndOptions.deleteCharAt(columnsAndOptions.length() - 1);
        values.deleteCharAt(values.length() - 1);
        String sql = b.insert(columnsAndOptions.toString(), values.toString()).build();
        execSQL(sql);
    }

    public void saveAll(List<Object> objects) {
        if (objects != null) {
            for (Object object : objects) {
                save(object);
            }
        }
    }

    public void deleteAll(Class<?> clz) {
        execSQL("DROP TABLE " + clz.getSimpleName());
    }

    public void deleteById(Class<?> clz, int id) {
        execSQL("DELETE FROM " + clz.getSimpleName() + " WHERE id = " + id);
    }

    public void delete(Class<?> clz, WhereBuilder b) {
        mDb.delete(clz.getSimpleName(), b.getWb(), b.getVb());
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void update(Object object, String... columns) {
        if (object != null) {
            ContentValues values = new ContentValues();
            Field[] declaredFields = object.getClass().getDeclaredFields();

            int id = 0;
            try {
                Field f = object.getClass().getDeclaredField("id");
                f.setAccessible(true);
                id = f.getInt(object);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                String name = declaredField.getName();
                boolean shouldUpdate = false;
                if (columns == null) {
                    shouldUpdate = true;
                } else {
                    for (String column : columns) {
                        if (column.equals(name)) {
                            shouldUpdate = true;
                            break;
                        }
                    }
                }
                if (shouldUpdate) {
                    String type = declaredField.getType().getSimpleName().toLowerCase();
                    switch (type) {
                        case "string":
                            try {
                                values.put(name, (String) declaredField.get(object));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "int":
                            try {
                                values.put(name, declaredField.getInt(object));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "boolean":
                            try {
                                values.put(name, declaredField.getBoolean(object) ? 1 : 0);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "long":
                            try {
                                values.put(name, declaredField.getLong(object));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            try {
                                values.put(name, "" + declaredField.get(object));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }

            }
            mDb.update(object.getClass().getSimpleName(), values, "id = ?", new String[]{"" + id});
        }
    }

    public void update(int id, Object object, String... columns) {
        ContentValues values = new ContentValues();
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            String name = declaredField.getName();
            boolean shouldUpdate = false;
            if (columns == null) {
                shouldUpdate = true;
            } else {
                for (String column : columns) {
                    if (column.equals(name)) {
                        shouldUpdate = true;
                        break;
                    }
                }
            }
            if (shouldUpdate) {
                String type = declaredField.getType().getSimpleName().toLowerCase();
                switch (type) {
                    case "string":
                        try {
                            values.put(name, (String) declaredField.get(object));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "int":
                        try {
                            values.put(name, declaredField.getInt(object));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "boolean":
                        try {
                            values.put(name, declaredField.getBoolean(object) ? 1 : 0);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "long":
                        try {
                            values.put(name, declaredField.getLong(object));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        try {
                            values.put(name, "" + declaredField.get(object));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }

        }
        mDb.update(object.getClass().getSimpleName(), values, "id = ?", new String[]{"" + id});
    }

    public static class WhereBuilder {
        private StringBuilder wb;
        private List<String> vb;

        public WhereBuilder() {
            wb = new StringBuilder();
            vb = new ArrayList<>();
        }

        public WhereBuilder(String column, String condition, String value) {
            wb = new StringBuilder();
            vb = new ArrayList<>();
            wb.append(column).append(" ").append(condition).append(" ? ");
            vb.add(value);
        }

        public WhereBuilder and(String column, String condition, String value) {
            wb.append(" AND ").append(column).append(" ").append(condition).append(" ? ");
            vb.add(value);
            return this;
        }

        public WhereBuilder or(String column, String condition, String value) {
            wb.append(" OR ").append(column).append(" ").append(condition).append(" ? ");
            vb.add(value);
            return this;
        }

        public String[] getVb() {
            String[] strings = new String[vb.size()];
            for (int i = 0; i < vb.size(); i++) {
                strings[i] = vb.get(i);
            }
            return strings;
        }

        public String getWb() {
            return wb.toString();
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public <T> List<T> findAll(Class<T> clz, String sql) {
        List<T> list = new ArrayList<>();
        Cursor cursor = findAll(sql);
        while (cursor.moveToNext()) {
            try {
                T t = CursorUtils.getEntity(clz, cursor);
                list.add(t);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        Logger.d(list.toString());
        return list;
    }

    public Cursor findAll(String sql) {
        return mDb.rawQuery(sql, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public <T> List<T> findAll(Class<T> clz, WhereBuilder b) {
        List<T> list = new ArrayList<>();
        Cursor cursor = findAll(clz.getSimpleName(), b);
        while (cursor.moveToNext()) {
            try {
                T t = CursorUtils.getEntity(clz, cursor);
                list.add(t);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        Logger.d(list.toString());
        return list;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public <T> List<T> findAll(Class<T> clz) {
        List<T> list = new ArrayList<>();
        String  sql = "SELECT * FROM "+clz.getSimpleName();
        Logger.d(sql);
        Cursor cursor = findAll(sql);
        while (cursor.moveToNext()) {
            try {
                T t = CursorUtils.getEntity(clz, cursor);
                list.add(t);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        Logger.d(list.toString());
        return list;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public <T> T findFirst(Class<T> clz) {
        T t = null;
        String  sql = "SELECT * FROM "+clz.getSimpleName();
        Logger.d(sql);
        Cursor cursor = findAll(sql);
        if (cursor.moveToFirst()) {
            try {
                t = CursorUtils.getEntity(clz, cursor);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        if (!cursor.isClosed())
            cursor.close();
        return t;
    }

}
