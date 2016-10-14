package me.zhouzhuo.zzsqlhelper.utils;

import android.util.Log;

/**
 * Created by zz on 2016/10/14.
 */

public class Logger {

    private static final String TAG = "ZzSQLHelper";
    private static boolean enable = false;

    public static boolean isEnable() {
        return enable;
    }

    public static void enable(boolean enable) {
        Logger.enable = enable;
    }

    public static void e(String msg) {
        if (enable) {
            Log.e(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (enable) {
            Log.d(TAG, msg);
        }
    }

}
