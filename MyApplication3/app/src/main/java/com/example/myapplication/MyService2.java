package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MyService2 extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
