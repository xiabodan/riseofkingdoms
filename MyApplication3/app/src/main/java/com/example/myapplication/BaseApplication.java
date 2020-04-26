package com.example.myapplication;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();
        context = this;
    }
}