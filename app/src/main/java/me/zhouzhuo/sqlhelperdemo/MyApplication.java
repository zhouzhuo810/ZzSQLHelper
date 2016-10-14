package me.zhouzhuo.sqlhelperdemo;

import android.app.Application;

import me.zhouzhuo.zzsqlhelper.utils.Logger;

/**
 * Created by zz on 2016/10/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Logger.enable(true);
    }
}
