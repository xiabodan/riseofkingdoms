package com.example.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class AppUtils {
    private AppUtils() {
    }

    public static synchronized String getAppName() {
        String charSequence;
        synchronized (AppUtils.class) {
            try {
                PackageManager packageManager = getContext().getPackageManager();
                charSequence = packageManager.getApplicationLabel(packageManager.getApplicationInfo(getContext().getPackageName(), 128)).toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return charSequence;
    }

    public static Context getContext() {
        return BaseApplication.getContext();
    }

    public static String getCountry() {
        try {
            return ((TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSimCountryIso().toUpperCase();
        } catch (Exception e) {
            e.getMessage();
            return "";
        }
    }

    public static String getLanguage() {
        try {
            return getContext().getResources().getConfiguration().locale.getLanguage();
        } catch (Exception e) {
            e.getMessage();
            return "";
        }
    }

    public static String getSysCountry() {
        try {
            return getContext().getResources().getConfiguration().locale.getCountry();
        } catch (Exception e) {
            e.getMessage();
            return "";
        }
    }

    public static int getVersionCode() {
        int i = 0;
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), i).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public static String getVersionName() {
        try {
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
