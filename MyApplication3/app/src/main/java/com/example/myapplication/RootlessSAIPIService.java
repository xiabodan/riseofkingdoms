package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;
import android.util.Log;

public class RootlessSAIPIService extends Service {
    private static final String TAG = "RootlessSAIPIService";

    public static final String ACTION_INSTALLATION_STATUS_NOTIFICATION = "com.aefyr.sai.action.INSTALLATION_STATUS_NOTIFICATION";
    public static final String EXTRA_INSTALLATION_STATUS = "com.aefyr.sai.extra.INSTALLATION_STATUS";
    public static final String EXTRA_SESSION_ID = "com.aefyr.sai.extra.SESSION_ID";
    public static final String EXTRA_PACKAGE_NAME = "com.aefyr.sai.extra.PACKAGE_NAME";
    public static final String EXTRA_ERROR_DESCRIPTION = "com.aefyr.sai.extra.ERROR_DESCRIPTION";

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_CONFIRMATION_PENDING = 1;
    public static final int STATUS_FAILURE = 2;


    public RootlessSAIPIService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                Log.d(TAG, "Requesting user confirmation for installation " + confirmationIntent);
                ConfirmationIntentWrapperActivity.start(this, intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1), confirmationIntent);
                break;
            case PackageInstaller.STATUS_SUCCESS:
                Log.d(TAG, "Installation succeed");
                break;
            default:
                Log.d(TAG, "Installation failed");
                break;
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
