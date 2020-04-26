package com.example.myapplication;


public class ResourceUtil {
    public static int getLayoutId(String str) {
        return getResByType(str, "layout");
    }

    public static int getResAnm(String str) {
        return getResByType(str, "anim");
    }

    private static int getResByType(String str, String str2) {
        return AppUtils.getContext().getResources().getIdentifier(str, str2, AppUtils.getContext().getPackageName());
    }

    public static int getResCol(String str) {
        return getResByType(str, "color");
    }

    public static int getResDraw(String str) {
        return getResByType(str, "drawable");
    }

    public static int getResId(String str) {
        return getResByType(str, "id");
    }

    public static int getResMipmap(String str) {
        return getResByType(str, "mipmap");
    }

    public static int getResStr(String str) {
        return getResByType(str, "string");
    }

//    public static int getResStyle(String str) {
//        return getResByType(str, AnalyticsEvents.PARAMETER_LIKE_VIEW_STYLE);
//    }
}