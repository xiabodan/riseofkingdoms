package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class SandboxedProcessService2 extends Service {
    public SandboxedProcessService2() {
    }
    public class MyBinder extends Binder {
        public SandboxedProcessService2 getService(){
            return SandboxedProcessService2.this;
        }
    }

    private SandboxedProcessService2.MyBinder binder = new SandboxedProcessService2.MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }
}
