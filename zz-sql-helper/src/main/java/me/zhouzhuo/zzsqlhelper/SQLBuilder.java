package me.zhouzhuo.zzsqlhelper;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;

import me.zhouzhuo.zzsqlhelper.anotation.Column;
import me.zhouzhuo.zzsqlhelper.utils.Logger;

/**
 * Created by zz on 2016/10/13.
 */

public class SQLBuilder {

    private String tableName;
    private static final String SELECT = "SELECT * FROM ";
    private static final String INSERT = "INSERT INTO ";
    private static final String DELETE = "DELETE FROM ";
    private static final String VALUES = " VALUES ";
    private static final String UPDATE = "UPDATE ";
    private static final String SET = " SET ";
    private static final String WHERE = " WHERE ";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String LIMIT = " LIMIT ";
    private static final String DESC = " DESC ";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";
    private static final String ID = "(id INTEGER NOT NULL";
    private static final String VARCHAR = " VARCHAR(255) ";
    private static final String INTEGER = " INTEGER(11) ";

    private StringBuilder b;
    private Class<?> clz;

    public SQLBuilder() {
        b = new StringBuilder();
    }

    public SQLBuilder from(Class<?> clz) {
        this.clz = clz;
        this.tableName = clz.getSimpleName().toLowerCase();
        return this;
    }

    public SQLBuilder createTable() {
        this.b.append(CREATE_TABLE).append(tableName).append(ID);
        Field[] declaredFields = clz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String name = declaredField.getName();
            Column annotation = declaredField.getAnnotation(Column.class);
            if (annotation == null || annotation.save()) {
                if (!name.equals("id") && !name.contains("$") && !name.equals("serialVersionUID")) {
                    if (declaredField.getType().getSimpleName().toLowerCase().equals("boolean")) {
                        b.append(",").append(name).append(INTEGER);
                    } else {
                        b.append(",").append(name).append(VARCHAR);
                    }
                }
            }
        }
        b.append(", PRIMARY KEY (id))");
        return this;
    }

    public SQLBuilder insert(String columns, String values) {
        this.b.append(INSERT).append(tableName)
                .append(" (").append(columns).append(")")
                .append(VALUES).append("(").append(values).append(")");
        return this;
    }

    public SQLBuilder insert(String[] columns, String[] values) {
        this.b.append(INSERT).append(tableName)
                .append(" (").append(TextUtils.join(",",columns)).append(")")
                .append(VALUES).append("(").append("'").append(TextUtils.join("','",values)).append("'").append(")");
        return this;
    }

    public SQLBuilder select() {
        b.append(SELECT).append(tableName);
        return this;
    }

    public SQLBuilder delete() {
        b.append(DELETE).append(tableName);
        return this;
    }

    public SQLBuilder delete(String tableName) {
        b.append(DELETE).append(tableName);
        return this;
    }

    public SQLBuilder update(String[] columns, String[] values) {
        b.append(UPDATE).append(tableName)
                .append(SET);
        if (columns.length == values.length) {
            for (int i = 0; i < columns.length; i++) {
                if (i != columns.length - 1) {
                    b.append(columns[i]).append("=").append("'").append(values[i]).append("'").append(",");
                } else {
                    b.append(columns[i]).append("=").append("'").append(values[i]).append("'");
                }
            }
        }
        return this;
    }

    public SQLBuilder update(String tableName, String[] columns, String[] values) {
        b.append(UPDATE).append(tableName)
                .append(SET);
        if (columns.length == values.length) {
            for (int i = 0; i < columns.length; i++) {
                if (i != columns.length - 1) {
                    b.append(columns[i]).append("=").append(values[i]).append(",");
                } else {
                    b.append(columns[i]).append("=").append(values[i]);
                }
            }
        }
        return this;
    }

    public SQLBuilder where(String column, String option, Object value) {
        if (value instanceof String) {
            this.b.append(WHERE).append(column).append(option).append("'").append(value).append("'");
        } else {
            this.b.append(WHERE).append(column).append(option).append(value);
        }
        return this;
    }

    public SQLBuilder and(String column, String option, String value) {
        this.b.append(AND).append(column).append(option).append(value);
        return this;
    }

    public SQLBuilder or(String column, String option, String value) {
        this.b.append(OR).append(column).append(option).append(value);
        return this;
    }

    public SQLBuilder orderBy(String column, boolean desc) {
        this.b.append(ORDER_BY).append(column);
        if (desc) {
            b.append(DESC);
        }
        return this;
    }

    public SQLBuilder limit(int limit) {
        this.b.append(LIMIT).append(limit);
        return this;
    }

    public String build() {
        Logger.d(b.toString());
        return this.b.toString();
    }


}
