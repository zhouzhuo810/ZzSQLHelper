package me.zhouzhuo.zzsqlhelper.utils;

import android.database.Cursor;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;

import me.zhouzhuo.zzsqlhelper.anotation.Column;

/**
 * Created by zz on 2016/10/13.
 */

public class CursorUtils {

    /**
     * ORM
     * convert cursor to entity
     *
     * @param clz    class type
     * @param cursor cursor
     * @param <T>    T
     * @return T
     * @throws ReflectiveOperationException exception
     */
    public static <T> T getEntity(Class<T> clz, Cursor cursor) throws ReflectiveOperationException {
        T t = clz.newInstance();
        int columnCount = cursor.getColumnCount();
        Log.e("xxx", columnCount + "");
        for (int i = 0; i < columnCount; i++) {
            String columnName = cursor.getColumnName(i);
            Field declaredField = t.getClass().getDeclaredField(columnName);
            declaredField.setAccessible(true);
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation == null || annotation.save()) {
                Class<?> type = declaredField.getType();
                switch (type.getSimpleName().toLowerCase()) {
                    case "string":
                        String value = cursor.getString(i);
                        declaredField.set(t, value);
                        break;
                    case "int":
                        int value1 = cursor.getInt(i);
                        declaredField.setInt(t, value1);
                        break;
                    case "boolean":
                        int value6 = cursor.getInt(i);
                        declaredField.setBoolean(t, value6 == 1);
                        break;
                    case "float":
                        float value2 = cursor.getFloat(i);
                        declaredField.setFloat(t, value2);
                        break;
                    case "long":
                        Long value3 = cursor.getLong(i);
                        declaredField.setLong(t, value3);
                        break;
                    case "double":
                        Double value4 = cursor.getDouble(i);
                        declaredField.setDouble(t, value4);
                        break;
                    case "short":
                        Short value5 = cursor.getShort(i);
                        declaredField.setShort(t, value5);
                        break;
                }
            }
        }
        return t;
    }


    public static void print(Cursor cursor) {
        if (cursor != null) {
            if (Logger.isEnable()) {
                StringBuilder b = new StringBuilder();
                while (cursor.moveToNext()) {
                    int count = cursor.getColumnCount();
                    b.append("Cursor content : {");
                    for (int i = 0; i < count; i++) {
                        int type = cursor.getType(i);
                        switch (type) {
                            case Cursor.FIELD_TYPE_STRING:
                                b.append(cursor.getColumnName(i)).append("=").append(cursor.getString(i)).append(", ");
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                b.append(cursor.getColumnName(i)).append("=").append(cursor.getInt(i)).append(", ");
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                b.append(cursor.getColumnName(i)).append("=").append(cursor.getFloat(i)).append(", ");
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                try {
                                    b.append(cursor.getColumnName(i)).append("=").append(new String(cursor.getBlob(i), "utf-8")).append(", ");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                    if (count > 0) {
                        b.deleteCharAt(b.length() - 2);
                        b.deleteCharAt(b.length() - 1);
                    }
                    b.append("}\n ");
                }
                Logger.d(b.toString());
            }
        }
    }
}
