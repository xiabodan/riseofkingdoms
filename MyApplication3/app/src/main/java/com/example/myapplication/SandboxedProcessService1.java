package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SandboxedProcessService1 extends Service {
    public SandboxedProcessService1() {
    }

    public class MyBinder extends Binder {
        public SandboxedProcessService1 getService(){
            return SandboxedProcessService1.this;
        }
    }

    private SandboxedProcessService1.MyBinder binder = new SandboxedProcessService1.MyBinder();
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }
}
