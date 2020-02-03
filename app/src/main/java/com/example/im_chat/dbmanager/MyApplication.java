package com.example.im_chat.dbmanager;

import android.app.Application;
import android.content.Context;

/**
 *   初始化本地数据库
 * @auther songjihu
 * @since 2020/2/2 13:24
 */
public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        //GreenDaoManager.getInstance();
    }

    public static Context getContext() {
        return mContext;
    }
}

