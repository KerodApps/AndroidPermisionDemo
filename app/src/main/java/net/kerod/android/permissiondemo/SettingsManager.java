package net.kerod.android.permissiondemo;

import android.content.Context;
import android.preference.PreferenceManager;

public class SettingsManager {
    private static Context context = ApplicationManager.getAppContext();



    public static boolean isPermissionRequested(String permission) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(permission, false);
    }

    public static void setPermissionRequested(String permission, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(permission, value).commit();
    }


}


