package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

public class MyService extends Service {
    public MyService() {
    }

    public class MyBinder extends Binder {

        public MyService getService(){
            return MyService.this;
        }

    }
    private MyBinder binder = new MyBinder();

    public class MyBinder2 extends IMyAidlInterface2.Stub {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
            Log.i("xiabo", "MyBinder2 basicTypes " + anInt);
        }

        @Override
        public void testBinder2(int test) {
            Log.i("xiabo", "MyBinder2 testBinder2 " + test);
        }
    }
    private MyBinder2 binder2 = new MyBinder2();

    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface.Stub() {
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
                Log.i("xiabo", "MyBinder1 basicTypes " + anInt);
            }

            @Override
            public IBinder getIBinder(int test) {
                Log.i("xiabo", "MyBinder1 getIBinder " + test);
                return binder2.asBinder();
            }
        };
    }
}
