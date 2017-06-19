package net.kerod.android.permissiondemo;

import android.app.Application;
import android.content.Context;



public class ApplicationManager extends Application {

    private static Context mContext;
    private static final String TAG = "ApplicationManager";

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

}