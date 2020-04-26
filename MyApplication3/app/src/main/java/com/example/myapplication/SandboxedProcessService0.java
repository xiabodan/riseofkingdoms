package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SandboxedProcessService0 extends Service {
    public SandboxedProcessService0() {
    }

    public class MyBinder extends Binder {
        public SandboxedProcessService0 getService(){
            return SandboxedProcessService0.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }
}
